package za.co.entelect.challenge;

import za.co.entelect.challenge.command.*;
import za.co.entelect.challenge.entities.*;
import za.co.entelect.challenge.enums.PowerUps;
import za.co.entelect.challenge.enums.Terrain;

import java.util.*;

import static java.lang.Math.max;

// import java.lang.constant.DirectMethodHandleDesc;

public class Bot {

    private static final int maxSpeed = 9;
    private Random random;
    private GameState gameState;
    private Car opponent;
    private Car myCar;

    private final static Command ACCELERATE = new AccelerateCommand();
    private final static Command LIZARD = new LizardCommand();
    // private final static Command NOTHING = new DoNothingCommand();
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

        if (checkLaneClearance(myCar)) {
            Command ordersLane = findClearLane(myCar);
            return ordersLane;
        } else {
            if (hasPowerUp(PowerUps.BOOST, myCar.powerups)) {
                return BOOST;
            }
        }

        if (isPlayerInFront(opp_pos, player_pos) == "front") {
            if (hasPowerUp(PowerUps.TWEET, myCar.powerups)) {
                return new TweetCommand(opp_pos.lane, opp_pos.block+opponent.speed+1);
            }

            if (hasPowerUp(PowerUps.BOOST, myCar.powerups)) {
                return BOOST;
            }

            if (hasPowerUp(PowerUps.LIZARD, myCar.powerups)) {
                return LIZARD;
            }

            return ACCELERATE;

        } else if (isPlayerInFront(opp_pos, player_pos) == "back") {
            if (hasPowerUp(PowerUps.EMP, myCar.powerups)) {
                return EMP;
            } 

            if (hasPowerUp(PowerUps.LIZARD, myCar.powerups)) {
                return LIZARD;
            } 

            if (hasPowerUp(PowerUps.BOOST, myCar.powerups)) {
                return BOOST;
            }
            if (myCar.powerups.length < 6) {
                Command cmd = findPowerUps(myCar);
                return cmd;
            }
            if (checkLaneClearance(myCar)) {
                Command ordersLane = findClearLane(myCar);
                return ordersLane;
            }
            return ACCELERATE;
        } else {
            if (myCar.speed == maxSpeed) {
                if (hasPowerUp(PowerUps.OIL, myCar.powerups)) {
                    return OIL;
                }
                if (hasPowerUp(PowerUps.EMP, myCar.powerups)) {
                    return EMP;
                }
            }

            if (hasPowerUp(PowerUps.BOOST, myCar.powerups)) {
                return BOOST;
            }

            if (hasPowerUp(PowerUps.LIZARD, myCar.powerups)) {
                return LIZARD;
            }
        }
        return ACCELERATE;
    }

    private boolean checkLaneClearance(Car myCar) {
        int myPosLane = myCar.position.lane;
        int myPosBlock = myCar.position.block;
        List<Object> sameLane = getInfoinLaneBased(myPosLane, myPosBlock, 1);
        List<Object> sameCT = getCTinLaneBased(myPosLane, myPosBlock, 1);
        if ((sameLane.contains(Terrain.WALL)) || (sameLane.contains(Terrain.MUD)) || (sameLane.contains(Terrain.OIL_SPILL)) || checkCyberTruck(sameCT)) {
            return true;
        } else {
            return false;
        }
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

        List<Object> sameLane = new ArrayList<Object>();
        List<Object> rightLane = new ArrayList<Object>();
        List<Object> leftLane = new ArrayList<Object>();
        if (myPosLane == 1) {
            sameLane = getInfoinLaneBased(myPosLane, myPosBlock, 1);
            rightLane = getInfoinLaneBased(myPosLane, myPosBlock, 2);
        } else if (myPosLane == 4) {
            leftLane = getInfoinLaneBased(myPosLane, myPosBlock, 0);
            sameLane = getInfoinLaneBased(myPosLane, myPosBlock, 1);
        } else {
            leftLane = getInfoinLaneBased(myPosLane, myPosBlock, 0);
            sameLane = getInfoinLaneBased(myPosLane, myPosBlock, 1);
            rightLane = getInfoinLaneBased(myPosLane, myPosBlock, 2);
        }

        // Iterating each lane to find the closest index
        // Iterate lane 1
        if ((leftLane.contains(Terrain.BOOST)) || (leftLane.contains(Terrain.EMP)) || (leftLane.contains(Terrain.LIZARD)) || (leftLane.contains(Terrain.OIL_POWER)) || (leftLane.contains(Terrain.TWEET))) {
            for (i = 0; i < leftLane.size(); i++) {
                if ((leftLane.get(i) == Terrain.BOOST) || (leftLane.get(i) == Terrain.EMP) || (leftLane.get(i) == Terrain.LIZARD) || (leftLane.get(i) == Terrain.OIL_POWER) || (leftLane.get(i) == Terrain.TWEET)) {
                    leftLanePowerUps = i;
                    lanePowerUps.add(leftLanePowerUps);
                    break;
                }
            }
        }

        if ((sameLane.contains(Terrain.BOOST)) || (sameLane.contains(Terrain.EMP)) || (sameLane.contains(Terrain.LIZARD)) || (sameLane.contains(Terrain.OIL_POWER)) || (sameLane.contains(Terrain.TWEET))) {
            for (i = 0; i < sameLane.size(); i++) {
                if ((sameLane.get(i) == Terrain.BOOST) || (sameLane.get(i) == Terrain.EMP) || (sameLane.get(i) == Terrain.LIZARD) || (sameLane.get(i) == Terrain.OIL_POWER) || (sameLane.get(i) == Terrain.TWEET)) {
                    sameLanePowerUps = i;
                    lanePowerUps.add(sameLanePowerUps);
                    break;
                }
            }
        }

        if ((rightLane.contains(Terrain.BOOST)) || (rightLane.contains(Terrain.EMP)) || (rightLane.contains(Terrain.LIZARD)) || (rightLane.contains(Terrain.OIL_POWER)) || (rightLane.contains(Terrain.TWEET))) {
            for (i = 0; i < rightLane.size(); i++) {
                if ((rightLane.get(i) == Terrain.BOOST) || (rightLane.get(i) == Terrain.EMP) || (rightLane.get(i) == Terrain.LIZARD) || (rightLane.get(i) == Terrain.OIL_POWER) || (rightLane.get(i) == Terrain.TWEET)) {
                    rightLanePowerUps = i;
                    lanePowerUps.add(rightLanePowerUps);
                    break;
                }
            }
        }

        // Sort lanePowerUps dari besar ke kecil
        Collections.sort(lanePowerUps);
        if (lanePowerUps.size() != 0) {
            closestPowerUps = lanePowerUps.get(0);

            if (closestPowerUps == leftLanePowerUps) {
                return TURN_LEFT;
            } else if (closestPowerUps == sameLanePowerUps) {
                return ACCELERATE;
            } else if (closestPowerUps == rightLanePowerUps) {
                return TURN_RIGHT;
            }
        }
            
        
        return ACCELERATE;
    }

    private Command findClearLane(Car myCar) {
        int i, j;
        int myPosLane = myCar.position.lane;
        int myPosBlock = myCar.position.block;
        int firstLeftObstacle = 0;
        int firstRightObstacle = 0;
        
        List<Object> rightCT = new ArrayList<Object>();
        List<Object> leftCT = new ArrayList<Object>();
        List<Object> rightLane = new ArrayList<Object>();
        List<Object> leftLane = new ArrayList<Object>();

        if (myPosLane == 1) {
            rightLane = getInfoinLaneBased(myPosLane, myPosBlock, 2);
            rightCT = getCTinLaneBased(myPosLane, myPosBlock, 2);
        } else if (myPosLane == 4) {
            leftLane = getInfoinLaneBased(myPosLane, myPosBlock, 0);
            leftCT = getCTinLaneBased(myPosLane, myPosBlock, 0);
        } else {
            leftLane = getInfoinLaneBased(myPosLane, myPosBlock, 0);
            leftCT = getCTinLaneBased(myPosLane, myPosBlock, 0);
            rightCT = getCTinLaneBased(myPosLane, myPosBlock, 2);
            rightLane = getInfoinLaneBased(myPosLane, myPosBlock, 2);
        }

        if (myPosLane == 1) {
            if ((rightLane.contains(Terrain.WALL)) || (rightLane.contains(Terrain.MUD)) || (rightLane.contains(Terrain.OIL_SPILL)) || checkCyberTruck(rightCT)) {
                if ((hasPowerUp(PowerUps.LIZARD, myCar.powerups)) && (myCar.speed != 0)) {
                    return LIZARD;
                } else {
                    j = random.nextInt(2);
                    if (j == 1) {
                        return ACCELERATE;
                    } else if (j == 2) {
                        return TURN_RIGHT;
                    }
                }
            }
            return TURN_RIGHT;
        } else if (myPosLane == 4) {
            if ((leftLane.contains(Terrain.WALL)) || (leftLane.contains(Terrain.MUD)) || (leftLane.contains(Terrain.OIL_SPILL)) || checkCyberTruck(leftCT)) {
                if ((hasPowerUp(PowerUps.LIZARD, myCar.powerups)) && (myCar.speed != 0)){
                    return LIZARD;
                } else {
                    j = random.nextInt(2);
                    if (j == 1) {
                        return ACCELERATE;
                    } else if (j == 2) {
                        return TURN_LEFT;
                    }
                }
            }
            return TURN_LEFT;
        } else {
            for (i = 0; i < leftLane.size(); i++) {
                if ((leftLane.get(i) == Terrain.WALL) || (leftLane.get(i) == Terrain.MUD) || (leftLane.get(i) == Terrain.OIL_SPILL) || checkCyberTruck(leftCT)) {
                    firstLeftObstacle = i;
                    break;
                }
            }

            for (i = 0;i < rightLane.size(); i++) {
                if ((rightLane.get(i) == Terrain.WALL) || (rightLane.get(i) == Terrain.MUD) || (rightLane.get(i) == Terrain.OIL_SPILL) || checkCyberTruck(rightCT)) {
                    firstRightObstacle = i;
                    break;
                }
            }

            if ((firstLeftObstacle == 0) || (firstRightObstacle != 0)) {
                return TURN_LEFT;
            } else if ((firstLeftObstacle != 0) || (firstRightObstacle == 0)) {
                return TURN_RIGHT;
            } else if (firstLeftObstacle < firstRightObstacle) {
                return TURN_RIGHT;
            } else if (firstLeftObstacle > firstRightObstacle) {
                return TURN_LEFT;
            } else if ((firstLeftObstacle == 0) && (firstRightObstacle == 0)) {
                j = random.nextInt(2);
                if (j == 1) {
                    return TURN_LEFT;
                } else if (j == 2) {
                    return TURN_RIGHT;
                }
            } else if (firstLeftObstacle == firstRightObstacle) {
                if (hasPowerUp(PowerUps.LIZARD, myCar.powerups)) {
                    return LIZARD;
                } else {
                    return ACCELERATE;
                }
            }
        }
        return ACCELERATE;
    }

    private String isPlayerInFront(Position opp_pos, Position player_pos) {
        int res = player_pos.block - opp_pos.block;
        if (res > 1) {
            return "front";
        } else if (res < 0) {
            return "back";
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

    private Boolean checkCyberTruck(List<Object> blocks) {
        for (int i = 0; i < blocks.size(); i++) {
            if (blocks.get(i) == "true") {
                return true;
            }
        }
        return false;
    }

    private List<Object> getCTinLaneBased(int lane, int block, int whichLane) {
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
            Boolean var = laneList[i].isOccupiedByCyberTruck;
            if (var) {
                blocks.add("true");
            } else {
                blocks.add("false");
            }
            
        }

        return blocks;
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
