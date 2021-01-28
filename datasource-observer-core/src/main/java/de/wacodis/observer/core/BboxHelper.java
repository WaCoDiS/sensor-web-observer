package de.wacodis.observer.core;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.wacodis.observer.model.AbstractDataEnvelopeAreaOfInterest;
import de.wacodis.observer.model.WacodisJobDefinition;

@Component
public class BboxHelper {
	
	private Logger logger = LoggerFactory.getLogger(BboxHelper.class);
	
	@Autowired
	private List<JobFactory> jobFactories;
	
	public BboxHelper () {
		
	};
	
	public BboxHelper (List<JobFactory> jobFactories) {
		this.jobFactories = jobFactories;
	};

	public String appendAreaOfInterestToQuartzJobKey(WacodisJobDefinition job, String jobId) {
		List<Float> areaOfInterestExtent = job.getAreaOfInterest().getExtent();
		String extent = areaOfInterestExtent.stream()
		        .map(String::valueOf)
		        .collect(Collectors.joining(","));
		jobId = String.join("_", jobId, extent);
		
		return jobId;
	}

	public String getQuartzKeyPartWithoutBbox(JobDetail jobDetailCandidate) {
		// BBOX is appended to jobId as "_<BBOX>"
		// hence get last index of "_" to create substring
		
		JobKey key = jobDetailCandidate.getKey();
		String name = key.getName();
		
		int lastIndex = name.lastIndexOf("_");
		
		String substring = name.substring(0, lastIndex);
		
		return substring;
	}

	public boolean anyQuartzJobIntersectsNewWacodisJobBBOX(List<JobKey> possibleQuartzJobKeys,
			JobDetail jobDetailCandidate) {
		boolean intersects = false;
		
		try {
			String bbox_newWacodisJob = getBboxSubstringFromQuartzJobKey(jobDetailCandidate.getKey());
			
			for (JobKey jobKey : possibleQuartzJobKeys) {
				try {
					String quartzJobCandidateBboxString = getBboxSubstringFromQuartzJobKey(jobKey);
					
					if(intersects(bbox_newWacodisJob, quartzJobCandidateBboxString)) {
						intersects = true;
						break;
					}
					
				} catch (Exception e) {
					logger.error(e.getMessage());
				}
				
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}		
		
		return intersects;
	}

	private boolean intersects(String bboxString_newWacodisJob, String bboxString_quartzJob) {
		// input strings follow pattern: "minLon,minLat,maxLon,maxLat" in EPSG:4326
		
		logger.info("Spatially compare BBOXes {} and {}", bboxString_quartzJob, bboxString_newWacodisJob);
		
		// an intersection of two BBOXes is only true, if BBoxes are not disjoint and actually touch each other
		
		String[] bbox_wacodisJob_stringSplit = bboxString_newWacodisJob.split(",");
		String[] bbox_quartzJob_stringSplit = bboxString_quartzJob.split(",");
		
		// minLon, minLat, maxLon, maxLat
		Float[] bbox_wacodisJob = toFloatArray(bbox_wacodisJob_stringSplit);
		Float[] bbox_quartzJob = toFloatArray(bbox_quartzJob_stringSplit);
		
		return ! ( bbox_quartzJob[0] >= bbox_wacodisJob[2] || bbox_quartzJob[2] <= bbox_wacodisJob[0] || bbox_quartzJob[3] <= bbox_wacodisJob[1] || bbox_quartzJob[1] >= bbox_wacodisJob[3]);		
	}

	private Float[] toFloatArray(String[] bbox_stringSplit) {
		Float[] array = new Float[bbox_stringSplit.length];
		
		for (int i = 0; i < bbox_stringSplit.length; i++) {
			array[i] = Float.parseFloat(bbox_stringSplit[i]);
		}
		
		return array;
	}

	public String getBboxSubstringFromQuartzJobKey(JobKey key) throws Exception {
		// BBOX is appended to jobId as "_<BBOX>" where "<BBOX>" == "minLon,minLat,maxLon,maxLat"
		// hence get last index of "_" to create substring
		String name = key.getName();

		int lastIndex = name.lastIndexOf("_");

		if(lastIndex != -1) {
			return name.substring(lastIndex+1);
		}
		else {
			throw new Exception("The Quartz JobKey '" + key.toString() + "' does not include a BBOX extent.");
		}
	}

	public JobKey findQuartzJobKeyIntersectingNewWacodisJobBBOX(List<JobKey> possibleQuartzJobKeys,
			JobDetail jobDetailCandidate) {
		JobKey result = null;
		try {
			String bbox_newWacodisJob = getBboxSubstringFromQuartzJobKey(jobDetailCandidate.getKey());
			
			for (JobKey jobKey : possibleQuartzJobKeys) {
				try {
					String quartzJobCandidateBboxString = getBboxSubstringFromQuartzJobKey(jobKey);
					
					if(intersects(bbox_newWacodisJob, quartzJobCandidateBboxString)) {
						result = jobKey;
						break;
					}
					
				} catch (Exception e) {
					logger.error(e.getMessage());
				}
				
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}	
	
		return result;
	}

	public JobDetail expandBboxOfExistingQuartzJob(JobDetail existingQuartzJob, JobDetail jobDetail, String aoiKey) throws Exception {
		String bboxSubstring_wacodisJob = getBboxSubstringFromQuartzJobKey(jobDetail.getKey());
		String bboxSubstring_quartzJob = getBboxSubstringFromQuartzJobKey(existingQuartzJob.getKey());
		
		String expandedBbox = getExpandedBbox(bboxSubstring_quartzJob, bboxSubstring_wacodisJob);
		
		String quartzKeyPartWithoutBbox = getQuartzKeyPartWithoutBbox(existingQuartzJob);
		String newJobKeyName = String.join("_", quartzKeyPartWithoutBbox, expandedBbox);
		
		JobDataMap jobDataMap = existingQuartzJob.getJobDataMap();
		
		JobDetail newJobDetail = existingQuartzJob.getJobBuilder().withIdentity(newJobKeyName, existingQuartzJob.getKey().getGroup()).usingJobData(jobDataMap).storeDurably(true).build();
		
		JobDataMap jobDataMap_newJob = newJobDetail.getJobDataMap();
		
		newJobDetail = modifyJobDetailBboxForJobExecution(newJobDetail, expandedBbox, aoiKey);
		
		return newJobDetail;
	}

	private JobDetail modifyJobDetailBboxForJobExecution(final JobDetail jobDetail, String expandedBbox, String aoiKey) {
		
		// can only have one item, as jobDetail corresponds to exaclty one JobFactory
		List<JobFactory> factories = jobFactories.stream()
        .filter(jf -> jf.supportsJobDetail(jobDetail))
        .collect(Collectors.toList());
		
		factories.get(0).modifyBboxParameter(jobDetail, expandedBbox);
		
		JobDataMap jobDataMap = jobDetail.getJobDataMap();
		
		//data.put(AOI_KEY, job.getAreaOfInterest());
		AbstractDataEnvelopeAreaOfInterest areaOfInterest = new AbstractDataEnvelopeAreaOfInterest();
		Float[] extent = toFloatArray(expandedBbox.split(","));		
		areaOfInterest.setExtent(Arrays.asList(extent));
		jobDataMap.put(aoiKey, areaOfInterest);
		
		return jobDetail;		
	}

	private String getExpandedBbox(String bboxSubstring_quartzJob, String bboxSubstring_wacodisJob) {
		
		logger.info("Create expanded BBOX from input BBOXes {} and {}", bboxSubstring_quartzJob, bboxSubstring_wacodisJob);
		
		String[] bbox_wacodisJob_stringSplit = bboxSubstring_wacodisJob.split(",");
		String[] bbox_quartzJob_stringSplit = bboxSubstring_quartzJob.split(",");
		
		// minLon, minLat, maxLon, maxLat
		Float[] bbox_wacodisJob = toFloatArray(bbox_wacodisJob_stringSplit);
		Float[] bbox_quartzJob = toFloatArray(bbox_quartzJob_stringSplit);
		
		Float[] bbox_expanded = bbox_quartzJob;
		
		// minLon
		if(bbox_wacodisJob[0] < bbox_expanded[0]) {
			bbox_expanded[0] = bbox_wacodisJob[0];
		}
		// minLat
		if(bbox_wacodisJob[1] < bbox_expanded[1]) {
			bbox_expanded[1] = bbox_wacodisJob[1];
		}
		// maxLon
		if(bbox_wacodisJob[2] > bbox_expanded[2]) {
			bbox_expanded[2] = bbox_wacodisJob[2];
		}
		// maxLat
		if(bbox_wacodisJob[3] > bbox_expanded[3]) {
			bbox_expanded[3] = bbox_wacodisJob[3];
		}
		
		String extent = bbox_expanded[0] + "," + bbox_expanded[1] + "," + bbox_expanded[2] + "," + bbox_expanded[3];
		
		return extent;
	}
	
	

}
