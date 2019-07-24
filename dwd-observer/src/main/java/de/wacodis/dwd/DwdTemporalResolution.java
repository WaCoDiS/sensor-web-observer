package de.wacodis.dwd;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class DwdTemporalResolution {
	
	// Enumerations of temporal resolution
		// {average Temp., precipitation, air pressure, air humidity, cloud coverage}
		public Set hourly = new HashSet<>(Arrays.asList("TT_TU_MN009", "R1_MN008", "P0_MN008", "RF_TU_MN009", "N_MN008"));
		// {average Temp., max temp, min temp, precipitation, wind top, air pressure,
		// snow height, fresh snow height, sunshine duration, air humidity, cloud
		// coverage}
		public Set daily = new HashSet<>(Arrays.asList("TMK_MN004", "TXK_MN004", "TNK_MN004", "RS_MN006", "FX_MN003",
				"PM_MN004", "SH_TAG_MN006", "NSH_TAG_MN006", "SDK_MN004", "UPM_MN004", "NM_MN004"));
		// {average Temp., max temp, min temp, precipitation, air pressure, snow height,
		// fresh snow height, sunshine duration, air humidity, cloud coverage}
		public Set monthly = new HashSet<>(Arrays.asList("MO_TT_MN004", "MO_TX_MN004", "MO_TN_MN004", "MO_RR_MN006",
				"MO_P0_MN004", "MO_SH_S_MN006", "MO_NSH_MN006", "MO_SD_S_MN004", "MO_RF_MN004", "MO_N_MN004"));
		// {average Temp., max temp, min temp, precipitation, air pressure, snow height,
		// fresh snow height, sunshine duration, air humidity, cloud coverage}
		public Set annual = new HashSet<>(Arrays.asList("JA_TT_MN004", "JA_TX_MN004", "JA_TN_MN004", "JA_RR_MN006",
				"JA_P0_MN004", "JA_SH_S_MN006", "JA_NSH_MN006", "JA_SD_S_MN004", "JA_RF_MN004", "JA_N_MN004"));


}
