package uk.org.rockthehalo.intermud3.testsuite;

import uk.org.rockthehalo.intermud3.I3Exception;
import uk.org.rockthehalo.intermud3.Intermud3;
import uk.org.rockthehalo.intermud3.LPCData;
import uk.org.rockthehalo.intermud3.LPCData.LPCTypes;

public class MudModeTest {
	private static final boolean doTest = false;

	public MudModeTest() {
	}

	public void test() {
		LPCData array = new LPCData(LPCTypes.MIXEDARR);
		LPCData mapping = new LPCData(LPCTypes.MIXEDMAP);
		String mudmodeArray = "({\"startup-reply\",5,\"*rth\",0,\"You\",0,({({\"*rth\",\"1.2.3.4 5\",\"A ,}) test\",}),}),})";
		String mudmodeMapping = "({4,([\"a\":\"test\",1:({1,2,3,}),2:([1:\"one ,]) !one!\",]),]),})";
		String toMudModeArray, toMudModeMapping;

		if (doTest) {
			Intermud3.instance.logInfo("Array:       " + mudmodeArray);

			try {
				array.fromMudMode(mudmodeArray);
			} catch (I3Exception e) {
				e.printStackTrace();
			}

			Intermud3.instance
					.logInfo("fromMudMode: " + array.get().toString());
			toMudModeArray = array.toMudMode();
			Intermud3.instance.logInfo("toMudMode:   " + toMudModeArray);

			Intermud3.instance.logInfo("Mapping:     " + mudmodeMapping);

			try {
				mapping.fromMudMode(mudmodeMapping);
			} catch (I3Exception e) {
				e.printStackTrace();
			}

			Intermud3.instance.logInfo("fromMudMode: "
					+ mapping.get().toString());
			toMudModeMapping = mapping.toMudMode();
			Intermud3.instance.logInfo("toMudMode:   " + toMudModeMapping);
		}
	}
}
