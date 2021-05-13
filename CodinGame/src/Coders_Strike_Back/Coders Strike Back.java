import java.util.*;
import java.io.*;
import java.math.*;

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
class Player {

    /** Boost dAéja utilisé ?*/
    static boolean boostUsed = false;
    static int nextCPX;
    static int nextCPY;
    /** Nombre de tours restants sans moteur suite a une utilisation du shield ?*/
    static int shieldTime = 0;

    public static void loadData (int nextCheckpointX, int nextCheckpointY) {
        nextCPX = nextCheckpointX;
        nextCPY = nextCheckpointY;
    }

    public static void move (String thurst) {
        System.out.println(nextCPX + " " + nextCPY + " " + thurst);
    }

    /** Return true ssi la cible est à une distance du pod < a distanceMax */
    public static boolean proche(int xMoi, int yMoi, int xCible, int yCible, int distanceMax) {
        int distance = (int) Math.round(Math.sqrt((yCible - yMoi) * (yCible - yMoi) + (xCible-xMoi) * (xCible-xMoi)));
        System.err.println("distanceOpponent : " + distance + "\ndistanceMaxOpponent : " + distanceMax);
        return distance < distanceMax;
    }

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);

        // game loop
        while (true) {
            int x = in.nextInt();
            int y = in.nextInt();
            int nextCheckpointX = in.nextInt(); // x position of the next check point
            int nextCheckpointY = in.nextInt(); // y position of the next check point
            int nextCheckpointDist = in.nextInt(); // distance to the next checkpoint
            int nextCheckpointAngle = in.nextInt(); // angle between your pod orientation and the direction of the next checkpoint
            int opponentX = in.nextInt();
            int opponentY = in.nextInt();
            String thurst = " "; //puissance du moteur

            //DEBUG
            System.err.println("distanceCP : " + nextCheckpointDist);

            loadData(nextCheckpointX, nextCheckpointY);
            //3 Tours sans moteurs apres shield
            if(shieldTime > 0) {
                shieldTime--;
            }

            //Gestion de l'acceleration
            if (nextCheckpointAngle > 90
            || nextCheckpointAngle < -90) {
                thurst += "10";
            } else if(nextCheckpointDist < 2500) {
                if (nextCheckpointAngle < 40 && nextCheckpointAngle > -40) {
                    thurst += "50";
                } else {
                    thurst += "40";
                }
            } else if(nextCheckpointDist < 5000) {
                if(nextCheckpointAngle > 65 || nextCheckpointAngle < -65) {
                    thurst += "30";
                } else if(nextCheckpointAngle < 40 || nextCheckpointAngle > -40) {
                    thurst += "100";
                } else {
                    thurst += "60";
                }
            } else {
                thurst += "100";
            }

            //Choix entre shield, boost ou acceleration
            if(proche(x, y, opponentX, opponentY, 860) && (nextCheckpointDist < 2000)) {
                move("SHIELD");
                shieldTime = 3;
            } else if(!boostUsed
            && shieldTime == 0
            && nextCheckpointDist > 3000
            && nextCheckpointAngle == 0){
                move("BOOST");
                boostUsed = true;
            } else {
                move(thurst);
            }
        }
    }
}