package za.co.entelect.challenge;

import za.co.entelect.challenge.command.*;
import za.co.entelect.challenge.entities.*;
import za.co.entelect.challenge.enums.Direction;
import za.co.entelect.challenge.enums.PowerUps;
import za.co.entelect.challenge.enums.Terrain;

import java.util.*;

import static java.lang.Math.max;

import java.lang.constant.DirectMethodHandleDesc;

public class Bot {

    private static final int maxSpeed = 9;
    private Random random;
    private GameState gameState;
    private Car opponent;
    private Car myCar;

    private final static Command ACCELERATE = new AccelerateCommand();
    private final static Command LIZARD = new LizardCommand();
    private final static Command NOTHING = new DoNothingCommand();
    private final static Command OIL = new OilCommand();
    private final static Command BOOST = new BoostCommand();
    private final static Command EMP = new EmpCommand();
    private final static Command FIX = new FixCommand();
    private final static Command TURN_RIGHT = new ChangeLaneCommand(1);
    private final static Command TURN_LEFT = new ChangeLaneCommand(-1);

    public Bot(Random random, GameState gameState) {
        this.random = random;
        this.gameState = gameState;
        this.myCar = gameState.player;
        this.opponent = gameState.opponent;
    }

    public Command run() {
        List<Object> blocks = getInfoinLaneBased(myCar.position.lane, myCar.position.block, 1);
        // List<Object> nextBlocks = blocks.subList(0,1);
        Position player_pos = myCar.position;
        Position opp_pos = opponent.position;

        //Fix if too much damage
        if (myCar.damage >= 2) {
            return FIX;
        }

        //Accelerate first if going too slow
        if(myCar.speed <= 3) {
            return ACCELERATE;
        }

        // Check opponent position
        if(opp_pos.block == player_pos.block+1 && isSameLaneWithOpp(player_pos, opp_pos)) {
            if (player_pos.lane == 1) {
                return TURN_RIGHT;
            } else if (player_pos.lane == 4) {
                return TURN_LEFT;
            } else {
                return TURN_LEFT;
            }
        }

        if (isPlayerInFront(opp_pos, player_pos) == "front") {
            if (hasPowerUp(PowerUps.BOOST, myCar.powerups) || hasPowerUp(PowerUps.LIZARD, myCar.powerups) || hasPowerUp(PowerUps.OIL, myCar.powerups) || hasPowerUp(PowerUps.TWEET, myCar.powerups)) {
                if (hasPowerUp(PowerUps.BOOST, myCar.powerups)) {
                    return BOOST;
                } else if (hasPowerUp(PowerUps.LIZARD, myCar.powerups) || hasPowerUp(PowerUps.OIL, myCar.powerups) || hasPowerUp(PowerUps.TWEET, myCar.powerups) ) {
                    if (isSameLaneWithOpp(player_pos, opp_pos) && myCar.speed == maxSpeed) {
                        if (hasPowerUp(PowerUps.TWEET, myCar.powerups)) {
                            return new TweetCommand(opp_pos.lane, opp_pos.block+opponent.speed);
                        }
                        if (hasPowerUp(PowerUps.LIZARD, myCar.powerups)) {
                            return LIZARD;
                        }
                        if (hasPowerUp(PowerUps.OIL, myCar.powerups)) {
                            return OIL;
                        }
                    }
                }
                return ACCELERATE;
            } else {
                if (player_pos.lane == 2 || player_pos.lane == 3) {
                    Command cmd = findClearLane(myCar);
                    return cmd;
                }
            } 
        } else if (isPlayerInFront(opp_pos, player_pos) == "back") {
            if (hasPowerUp(PowerUps.BOOST, myCar.powerups)  || hasPowerUp(PowerUps.EMP, myCar.powerups)) {
                if (hasPowerUp(PowerUps.BOOST, myCar.powerups)) {
                    return BOOST;
                } else if (hasPowerUp(PowerUps.EMP, myCar.powerups)) {
                    if (hasPowerUp(PowerUps.EMP, myCar.powerups) && isCloseWithOpp(player_pos, opp_pos)) {
                        return EMP;
                    }
                }
                return ACCELERATE;
            } else {
                // if (player_pos.lane == 2 || player_pos.lane == 3) {
                //     Command cmd = findPowerUps(myCar);
                //     return cmd;
                // }
            }
        } else {
            return ACCELERATE;
        }
        return ACCELERATE;
        /*
            cek posisi musuh
            -> kalo kita di depan
                -> kalo kita punya powerup (boost, lizard, oil, tweet)
                    -> kalo jaraknya deket -> kalo ada boost -> boost
                    -> yg avail apa kasi itu
                    -> accel
                => kalo ga punya powerup 
                    -> cari powerup terdekat untuk menuju situ
                    -> accel
            -> kalo di belakang
            -   > kalo kita punya powerup (boost, tweet, emp)
                    -> kalo ada boost -> boost
                    -> kalo gaada penghalang di antara musuh sm kita -> kasi emp
                    -> accel
                => kalo ga punya powerup 
                    -> cari powerup terdekat untuk menuju situ
                    -> accel

            fungsi2
            -> cari powerup terdekat di lane/ lane sebelah
        */
    }

    private Boolean isSameLaneWithOpp(Position player_pos, Position opp_pos) {
        return (player_pos.lane == opp_pos.lane);
    }

    private Boolean isCloseWithOpp(Position player_pos, Position opp_pos) {
        return ((player_pos.lane == opp_pos.lane) || (player_pos.lane == opp_pos.lane-1) || (player_pos.lane == opp_pos.lane+1));
    }

    private Command findPowerUps(Car myCar) {
        int i;
        int myPosLane = myCar.position.lane;
        int myPosBlock = myCar.position.block;

        int leftLanePowerUps = 0;
        int sameLanePowerUps = 0;
        int rightLanePowerUps = 0;
        int closestPowerUps = 0;
        List<Integer> lanePowerUps = new ArrayList<Integer>();
        List<Object> leftLane = getInfoinLaneBased(myPosLane, myPosBlock, 0);
        List<Object> sameLane = getInfoinLaneBased(myPosLane, myPosBlock, 1);
        List<Object> rightLane = getInfoinLaneBased(myPosLane, myPosBlock, 2);
        List<Lane[]> map = gameState.lanes;
        int startBlock = map.get(0)[0].position.block;

        // Iterating each lane to find the closest index
        // Iterate lane 1
        if ((leftLane.contains(Terrain.BOOST)) || (leftLane.contains(Terrain.EMP)) || (leftLane.contains(Terrain.LIZARD)) || (leftLane.contains(Terrain.OIL_POWER)) || (leftLane.contains(Terrain.TWEET))) {
            for (i = max(myPosBlock - startBlock, 0); i < (leftLane.size() - myPosBlock); i++) {
                if ((leftLane.get(i) == Terrain.BOOST) || (leftLane.get(i) == Terrain.EMP) || (leftLane.get(i) == Terrain.LIZARD) || (leftLane.get(i) == Terrain.OIL_POWER) || (leftLane.get(i) == Terrain.TWEET)) {
                    leftLanePowerUps = i;
                    lanePowerUps.add(leftLanePowerUps);
                    break;
                }
            }
        }

        if ((sameLane.contains(Terrain.BOOST)) || (sameLane.contains(Terrain.EMP)) || (sameLane.contains(Terrain.LIZARD)) || (sameLane.contains(Terrain.OIL_POWER)) || (sameLane.contains(Terrain.TWEET))) {
            for (i = max(myPosBlock - startBlock, 0); i < (sameLane.size() - myPosBlock); i++) {
                if ((sameLane.get(i) == Terrain.BOOST) || (sameLane.get(i) == Terrain.EMP) || (sameLane.get(i) == Terrain.LIZARD) || (sameLane.get(i) == Terrain.OIL_POWER) || (sameLane.get(i) == Terrain.TWEET)) {
                    sameLanePowerUps = i;
                    lanePowerUps.add(sameLanePowerUps);
                    break;
                }
            }
        }

        if ((rightLane.contains(Terrain.BOOST)) || (rightLane.contains(Terrain.EMP)) || (rightLane.contains(Terrain.LIZARD)) || (rightLane.contains(Terrain.OIL_POWER)) || (rightLane.contains(Terrain.TWEET))) {
            for (i = max(myPosBlock - startBlock, 0); i < (rightLane.size() - myPosBlock); i++) {
                if ((rightLane.get(i) == Terrain.BOOST) || (rightLane.get(i) == Terrain.EMP) || (rightLane.get(i) == Terrain.LIZARD) || (rightLane.get(i) == Terrain.OIL_POWER) || (rightLane.get(i) == Terrain.TWEET)) {
                    rightLanePowerUps = i;
                    lanePowerUps.add(rightLanePowerUps);
                    break;
                }
            }
        }

        // Sort lanePowerUps dari besar ke kecil
        Collections.sort(lanePowerUps, Collections.reverseOrder());
        closestPowerUps = lanePowerUps.get(0);

        if (closestPowerUps == leftLanePowerUps) {
            return TURN_LEFT;
        } else if (closestPowerUps == sameLanePowerUps) {
            return ACCELERATE;
        } else if (closestPowerUps == rightLanePowerUps) {
            return TURN_RIGHT;
        }
        
        return ACCELERATE;
    }
    private Command findClearLane(Car myCar) {
        int i;
        int myPosLane = myCar.position.lane;
        int myPosBlock = myCar.position.block;
        int firstLeftObstacle = 0;
        int firstRightObstacle = 0;
        List<Object> leftLane = getInfoinLaneBased(myPosLane, myPosBlock, 0);
        List<Object> sameLane = getInfoinLaneBased(myPosLane, myPosBlock, 1);
        List<Object> rightLane = getInfoinLaneBased(myPosLane, myPosBlock, 2);
        List<Lane[]> map = gameState.lanes;
        int startBlock = map.get(0)[0].position.block;
        
        if ((sameLane.contains(Terrain.WALL) || sameLane.contains(Terrain.MUD))) {
            // Check lanes first
            if (myPosLane == 1) {
                return TURN_RIGHT;
            } else if (myPosLane == 4) {
                return TURN_LEFT;
            } else {
                for (i = max(myPosBlock - startBlock, 0); i < (leftLane.size() - myPosBlock); i++) {
                    if ((leftLane.get(i) == Terrain.WALL) || (leftLane.get(i) == Terrain.MUD) || (leftLane.get(i) == Terrain.OIL_SPILL)) {
                        firstLeftObstacle = i;
                        break;
                    }
                }

                for (i = max(myPosBlock - startBlock, 0);i < (rightLane.size() - myPosBlock);i++) {
                    if ((rightLane.get(i) == Terrain.WALL) || (rightLane.get(i) == Terrain.MUD) || (rightLane.get(i) == Terrain.OIL_SPILL)) {
                        firstRightObstacle = i;
                        break;
                    }
                }

                if (firstLeftObstacle <= firstRightObstacle) {
                    return TURN_RIGHT;
                } else {
                    return TURN_LEFT;
                }
            }
        }
        return ACCELERATE;
    }

    private Boolean isEmptyInDistanceOpp(List<Object> blocks, Position player_pos, Position opp_pos) {
        List<Lane[]> map = gameState.lanes;
        int startBlock = map.get(0)[0].position.block;
        for (int i = max(player_pos.block - startBlock, 0); i < opp_pos.block; i++) {
            if (blocks.get(i) == Terrain.WALL) {
                return false;
            }
        }
        return true;
    }

    private String isPlayerInFront(Position opp_pos, Position player_pos) {
        int res = player_pos.block - opp_pos.block;
        if (player_pos.lane == opp_pos.lane) {
            if (res >= 1) {
                return "front";
            } else if (res < 0) {
                return "back";
            }
        }
        return "invalid";
    }

    /**
     * Returns map of blocks and the objects in the for the current lanes, returns the amount of blocks that can be
     * traversed at max speed.
     **/
    private Boolean hasPowerUp(PowerUps powerUpToCheck, PowerUps[] available) {
        for (PowerUps powerUp: available) {
            if (powerUp.equals(powerUpToCheck)) {
                return true;
            }
        }
        return false;
    }

    private List<Object> getInfoinLaneBased(int lane, int block, int whichLane) {
        // Fungsi ini cuman bakal ambil lane yang diminta
        List<Lane[]> map = gameState.lanes; // lanes is the WORLD MAP
        List<Object> blocks = new ArrayList<>();
        int startBlock = map.get(0)[0].position.block;

        /*
        whichLane = 0 -> left lane
        whichLane = 1 -> current lane
        whichLane = 2 -> right lane */
        Lane[] laneList = {};
        if (whichLane == 0) {   
            laneList = map.get(lane - 2);
        } else if (whichLane == 1) {
            laneList = map.get(lane - 1);
        } else if (whichLane == 2) {
            laneList = map.get(lane);
        }

        for (int i = max(block - startBlock, 0); i <= block - startBlock + Bot.maxSpeed; i++) {
            if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
                break;
            }
            blocks.add(laneList[i].terrain);

        }
        return blocks;
    }
}
