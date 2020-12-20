package me.luvletter.planechess.client;

import javax.swing.*;
import java.awt.image.BufferedImage;

import static me.luvletter.planechess.util.Utility.*;

public class DiceAnimationHelper {
    public static BufferedImage getAnimationImage(int _count) {
        return switch (_count) {
            case 1 -> Resource.getResource(ResourceType.Dice_Rolling1);
            case 2 -> Resource.getResource(ResourceType.Dice_Rolling2);
            case 3 -> Resource.getResource(ResourceType.Dice_Rolling3);
            case 4 -> Resource.getResource(ResourceType.Dice_Rolling4);
            case 5 -> Resource.getResource(ResourceType.Dice_Rolling5);
            case 6 -> Resource.getResource(ResourceType.Dice_Rolling6);
            case 7 -> Resource.getResource(ResourceType.Dice_Rolling7);
            case 8 -> Resource.getResource(ResourceType.Dice_Rolling8);
            default -> Resource.getResource(ResourceType.Dice_Unknown);
        };
    }

    public static BufferedImage getResultImage(int result) {
        return switch (result) {
            case 1 -> Resource.getResource(ResourceType.Dice_Rolled1);
            case 2 -> Resource.getResource(ResourceType.Dice_Rolled2);
            case 3 -> Resource.getResource(ResourceType.Dice_Rolled3);
            case 4 -> Resource.getResource(ResourceType.Dice_Rolled4);
            case 5 -> Resource.getResource(ResourceType.Dice_Rolled5);
            case 6 -> Resource.getResource(ResourceType.Dice_Rolled6);
            default -> Resource.getResource(ResourceType.Dice_Unknown);
        };
    }

    public static void diceAnimate(Drawable_JPanel dpanel_Dice, int dice_result, int dice_round) {

        sleep(150);
        for (int i = 1; i < 9; i++) { // Animation loop
            dpanel_Dice.Draw(getAnimationImage(i));
           // sleep(300);
            // TODO: Debug
        }
        // the final loop, show result
        dpanel_Dice.Draw(getResultImage(dice_result));
    }

}
