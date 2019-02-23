package com.mmaengineer.engineerFishing;

import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.Vector3D;
import static org.osbot.rs07.api.map.constants.Banks.*;

public class ClosestBank {
    private static Area[] bankArray = new Area[]{EDGEVILLE, FALADOR_EAST, TZHAAR, LOVAKITE_MINE, ARCEUUS_HOUSE, CATHERBY, HOSIDIUS_HOUSE, FALADOR_WEST, SHAYZIEN_HOUSE, LOVAKENGJ_HOUSE,
            VARROCK_WEST, DRAYNOR, CANIFIS, ARDOUGNE_SOUTH, AL_KHARID, YANILLE, PEST_CONTROL, CASTLE_WARS, PISCARILIUS_HOUSE, GRAND_EXCHANGE, CAMELOT, GNOME_STRONGHOLD, ARDOUGNE_NORTH,
            LUMBRIDGE_LOWER, VARROCK_EAST, DUEL_ARENA, LUMBRIDGE_UPPER};
    private static Area newBank;
    private static int bankDistance = 10000000;

    public static Area getClosestBank(Position myPosition) {
        for (Area mapBank : bankArray) {
            int arrayBankDistance = myPosition.distance((Vector3D) mapBank);

            if (arrayBankDistance < bankDistance) {
                bankDistance = arrayBankDistance;
                newBank = mapBank;
            }
        }

        return newBank;
    }
}
