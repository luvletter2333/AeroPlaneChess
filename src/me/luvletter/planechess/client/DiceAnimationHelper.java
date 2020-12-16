package me.luvletter.planechess.client;

import javax.swing.*;
import java.awt.image.BufferedImage;

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

    public static void diceAnimate(JLabel label_status, Drawable_JPanel dpanel_Dice, int dice_result, int dice_round){
        try {
            Thread.sleep(150);
            for(int i=1;i<9;i++){ // Animation loop
                label_status.setText(convertToMultiline("Round " + dice_round + ".\nYou are dicing" + ".".repeat(i % 3 + 2)));
                dpanel_Dice.Draw(getAnimationImage(i));
                //Thread.sleep(300);
                //TODO: Debug Only!
            }
            //Thread.sleep(1000);
        } catch (InterruptedException interruptedException) {
            interruptedException.printStackTrace();
        }
        // the final loop, show result
        dpanel_Dice.Draw(getResultImage(dice_result));
        label_status.setText(convertToMultiline("Round " + dice_round + " ends.\nYou get " + dice_result + "!"));
    }

    private static String convertToMultiline(String orig) {
        return "<html>" + orig.replaceAll("\n", "<br>");
    }

}
