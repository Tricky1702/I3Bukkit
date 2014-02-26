package uk.org.rockthehalo.intermud3.testsuite;

import uk.org.rockthehalo.intermud3.Intermud3;
import uk.org.rockthehalo.intermud3.Packet;

public class MudModeTest {
	private static final boolean doTest = false;

	public MudModeTest() {
	}

	public void test() {
		Packet array = new Packet();
		Packet mapping = new Packet();
		String mudmodeArray = "({\"startup-reply\",5,\"*rth\",0,\"You\",0,({({\"*rth\",\"1.2.3.4 5\",\"A ,}) test\",}),}),})";
		String mudmodeMapping = "({4,([\"a\":\"test\",1:({1,2,3,}),2:([1:\"one ,]) !one!\",]),]),})";
		String toMudModeArray, toMudModeMapping;

		if (doTest) {
			Intermud3.instance.logInfo("Array:       " + mudmodeArray);
			array.fromMudMode(mudmodeArray);
			Intermud3.instance.logInfo("fromMudMode: " + array.toString());
			toMudModeArray = array.toMudMode();
			Intermud3.instance.logInfo("toMudMode:   " + toMudModeArray);

			Intermud3.instance.logInfo("Mapping:     " + mudmodeMapping);
			mapping.fromMudMode(mudmodeMapping);
			Intermud3.instance.logInfo("fromMudMode: " + mapping.toString());
			toMudModeMapping = mapping.toMudMode();
			Intermud3.instance.logInfo("toMudMode:   " + toMudModeMapping);
		}
	}
}
