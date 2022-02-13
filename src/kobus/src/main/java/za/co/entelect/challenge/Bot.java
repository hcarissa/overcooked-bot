package za.co.entelect.challenge;

import za.co.entelect.challenge.command.*;
import za.co.entelect.challenge.entities.*;
import za.co.entelect.challenge.enums.PowerUps;
import za.co.entelect.challenge.enums.Terrain;

import java.util.*;

import static java.lang.Math.max;

import java.security.SecureRandom;

public class Bot {

    private static final int maxSpeed = 9;
    private List<Command> directionList = new ArrayList<>();

    private final Random random;

    private final static Command ACCELERATE = new AccelerateCommand();
    private final static Command LIZARD = new LizardCommand();
    private final static Command OIL = new OilCommand();
    private final static Command BOOST = new BoostCommand();
    private final static Command EMP = new EmpCommand();
    private final static Command FIX = new FixCommand();

    private final static Command TURN_RIGHT = new ChangeLaneCommand(1);
    private final static Command TURN_LEFT = new ChangeLaneCommand(-1);

    public Bot() {
        this.random = new SecureRandom();
        directionList.add(TURN_LEFT);
        directionList.add(TURN_RIGHT);
    }

    public Command run(GameState gameState) {
        // Getting information
        List<Object> blocksAhead = getBlocksInFront(myCar.position.lane, myCar.position.block, gameState);
        List<Object> nextBlock = blocksAhead.subList(0,1);

        Car myCar = gameState.player;
        Car opponentCar = gameState.opponent;

        // Declaring player car status
        int currentSpeed = myCar.speed;
        int currentDamage = myCar.damage;

        // Side lane detection system (if the car is within lane 1 or 4)
        if (myCar.position == 1) {
            return TURN_RIGHT;
        }

        if (myCar.position == 4) {
            return TURN_LEFT;
        }
        
        //Basic avoidance logic
        findClearLane(myCar, gameState);

        // Tries to find the nearest power ups
        findPowerUps(myCar, gameState);

        //Basic fix logic
        

        //Fix first if too damaged to move
        if(myCar.damage == 5) {
            return FIX;
        }
        //Accelerate first if going to slow
        if(myCar.speed <= 3) {
            return ACCELERATE;
        }

        //Basic fix logic
        if(myCar.damage >= 5) {
            return FIX;
        }

        //Basic improvement logic
        if (hasPowerUp(PowerUps.BOOST, myCar.powerups)) {
            return BOOST;
        }

        //Basic aggression logic
        if (myCar.speed == maxSpeed) {
            if (hasPowerUp(PowerUps.OIL, myCar.powerups)) {
                return OIL;
            }
            if (hasPowerUp(PowerUps.EMP, myCar.powerups)) {
                return EMP;
            }
        }

        return ACCELERATE;

        // Aggresive mode
        // Check for TWEET availability
    }

    private Boolean hasPowerUp(PowerUps powerUpToCheck, PowerUps[] available) {
        for (PowerUps powerUp: available) {
            if (powerUp.equals(powerUpToCheck)) {
                return true;
            }
        }
        return false;
    }

    private int EnemyDistanceBlocks(Car myCar, Car opponentCar, GameState gameState) {
        int myPos = myCar.position.block;
        int opponentPos = opponentCar.position.block;

        if (myPos - opponentPos < 0) {
            return opponentPos - myPos;
        } else {
            return myPos - opponentPos;
        }
    }

    private int EnemyDistanceLane(Car myCar, Car opponentCar, GameState gameState) {
        int myPos = myCar.position.lane;
        int opponentPos = opponentCar.position.lane;

        if (myPos - opponentPos < 0) {
            return opponentPos - myPos;
        } else {
            return myPos - opponentPos;
        }
    }

    /*
    private Boolean checkPowerUpsAhead(Car myCar, GameState gameState) {
        List<Object> blocksAhead = getBlocksInFront(myCar.position.lane, myCar.position.block, gameState);

        if ((blocksAhead.contains(Terrain.BOOST)) || (blocksAhead.contains(Terrain.EMP)) || (blocksAhead.contains(Terrain.LIZARD)) || (blocksAhead.contains(Terrain.OIL_POWER)) || (blocksAhead.contains(Terrain.TWEET))){
            return true;
        } else {
            return false;
        }
    }*/

    private boolean moveLane(Car myCar, int lane) {
        int i;
        int myPosLane = myCar.position.lane;
        int currentSpeed = myCar.speed;
        // Jika lane sekarang berada di kiri lane yang dituju 
        if (myPosLane < lane) {
            for (i=0;i<lane-myPosLane;i++) {
                if (i != lane-myPosLane) {
                    return false;
                } else {
                    return true;
                }
            }
        } else if (myPosLane > lane) {
            for (i=0;i<myPosLane-lane;i++) {
                if (i != myPosLane-lane) {
                    return false;
                } else {
                    return true;
                }
            }
        }

        return false;
    }

    private Command findPowerUps(Car myCar, GameState gameState) {
        int i;
        int myPosLane = myCar.position.lane;
        int myPosBlock = myCar.position.block;
        /*
        int laneOnePowerUps = 0;
        int laneTwoPowerUps = 0;
        int laneThreePowerUps = 0;
        int laneFourPowerUps = 0;
        int closestPowerUps = 0;
        List<Integer> lanePowerUps = new ArrayList<Integer>();
        List<Object> laneOne = getInfoinLane(myPosLane, myPosBlock, gameState, 1);
        List<Object> laneTwo = getInfoinLane(myPosLane, myPosBlock, gameState, 2);
        List<Object> laneThree = getInfoinLane(myPosLane, myPosBlock, gameState, 3);
        List<Object> laneFour = getInfoinLane(myPosLane, myPosBlock, gameState, 4);*/

        int leftLanePowerUps = 0;
        int sameLanePowerUps = 0;
        int rightLanePowerUps = 0;
        List<Object> leftLane = getInfoinLaneBased(myPosLane, myPosBlock, gameState, 0);
        List<Object> sameLane = getInfoinLaneBased(myPosLane, myPosBlock, gameState, 1);
        List<Object> rightLane = getInfoinLaneBased(myPosLane, myPosBlock, gameState, 2);
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

        /*
        if ((laneFour.contains(Terrain.BOOST)) || (laneFour.contains(Terrain.EMP)) || (laneFour.contains(Terrain.LIZARD)) || (laneFour.contains(Terrain.OIL_POWER)) || (laneFour.contains(Terrain.TWEET))) {
            for (i = max(myPosBlock - startBlock, 0); i < (laneFour.size() - myPosBlock); i++) {
                if ((laneFour.get(i) == Terrain.BOOST) || (laneFour.get(i) == Terrain.EMP) || (laneFour.get(i) == Terrain.LIZARD) || (laneFour.get(i) == Terrain.OIL_POWER) || (laneFour.get(i) == Terrain.TWEET)) {
                    laneFourPowerUps = i;
                    lanePowerUps.add(laneFourPowerUps);
                    break;
                }
            }
        }*/

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

    private Command findClearLane(Car myCar, GameState gameState) {
        int i;
        int myPosLane = myCar.position.lane;
        int myPosBlock = myCar.position.block;
        int firstLeftObstacle = 0;
        int firstRightObstacle = 0;
        List<Object> leftLane = getInfoinLaneBased(myPosLane, myPosBlock, gameState, 0);
        List<Object> sameLane = getInfoinLaneBased(myPosLane, myPosBlock, gameState, 1);
        List<Object> rightLane = getInfoinLaneBased(myPosLane, myPosBlock, gameState, 2);
        List<Lane[]> map = gameState.lanes;
        int startBlock = map.get(0)[0].position.block;
        
        if ((sameLane.contains(Terrain.WALL)) || (sameLane.contains(Terrain.MUD)) || (sameLane.contains(Terrain.OIL_SPILL))) {
            // Check lanes first
            if (myPos == 1) {
                return TURN_RIGHT;
            } else if (myPos == 4) {
                return TURN_LEFT;
            } else {
                for (i = max(myPosBlock - startBlock, 0); i < (leftLane.size() - myPosBlock); i++) {
                    if (leftLane.get(i) == Terrain.WALL) {
                        firstLeftObstacle = i;
                        break;
                    }
                }

                for (i = max(myPosBlock - startBlock, 0);i < (rightLane.size() - myPosBlock);i++) {
                    if (rightLane.get(i) == Terrain.WALL) {
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

    /**
     * Returns map of blocks and the objects in the for the current lanes, returns
     * the amount of blocks that can be traversed at max speed.
     **/
    private List<Object> getBlocksInFront(int lane, int block, GameState gameState) {
        List<Lane[]> map = gameState.lanes; // lanes is the WORLD MAP
        List<Object> blocks = new ArrayList<>();
        int startBlock = map.get(0)[0].position.block;

        Lane[] laneList = map.get(lane - 1);
        for (int i = max(block - startBlock, 0); i <= block - startBlock + Bot.maxSpeed; i++) {
            if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
                break;
            }

            blocks.add(laneList[i].terrain);

        }
        return blocks;
    }

    private List<Object> getInfoinLaneBased(int lane, int block, GameState gameState, int whichLane) {
        // Fungsi ini cuman bakal ambil lane yang diminta
        List<Lane[]> map = gameState.lanes; // lanes is the WORLD MAP
        List<Object> blocks = new ArrayList<>();
        int startBlock = map.get(0)[0].position.block;

        /*
        whichLane = 0 -> left lane
        whichLane = 1 -> current lane
        whichLane = 2 -> right lane */
        if (whichLane == 0) {   
            Lane[] laneList = map.get(lane - 2);
        } else if (whichLane == 1) {
            Lane[] laneList = map.get(lane - 1);
        } else if (whichLane == 2) {
            Lane[] laneList = map.get(lane);
        }
        
        for (int i = max(block - startBlock, 0); i <= block - startBlock + Bot.maxSpeed; i++) {
            if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
                break;
            }

            blocks.add(laneList[i].terrain);

        }
        return blocks;
    }

    private List<Object> getInfoinLaneNonBased(int block, GameState gameState, int whichLane) {
        // Fungsi ini cuman bakal ambil lane yang diminta
        List<Lane[]> map = gameState.lanes; // lanes is the WORLD MAP
        List<Object> blocks = new ArrayList<>();
        int startBlock = map.get(0)[0].position.block;

        /*
        whichLane = 0 -> left lane
        whichLane = 1 -> current lane
        whichLane = 2 -> right lane */
        if (whichLane == 1) {   
            Lane[] laneList = map.get(0);
        } else if (whichLane == 2) {
            Lane[] laneList = map.get(1);
        } else if (whichLane == 3) {
            Lane[] laneList = map.get(2);
        } else if (whichLane == 4) {
            Lane[] laneList = map.get(3);
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
