package za.co.entelect.challenge;

import za.co.entelect.challenge.command.*;
import za.co.entelect.challenge.entities.*;
import za.co.entelect.challenge.enums.PowerUps;
import za.co.entelect.challenge.enums.State;
import za.co.entelect.challenge.enums.Terrain;

import java.util.*;

import static java.lang.Math.max;

import java.security.SecureRandom;

public class Bot {

    private static final int maxSpeed = 9;

    private Random random;
    private GameState gameState;
    private Car myCar;
    private Car opponentCar;

    private final static Command ACCELERATE = new AccelerateCommand();
    private final static Command LIZARD = new LizardCommand();
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
        this.opponentCar = gameState.opponent;
    }

    public Command run() {

        // Getting information
        List<Object> blocksAhead = getInfoinLaneBased(myCar.position.lane, myCar.position.block, 1);
        List<Object> nextBlock = blocksAhead.subList(0,1);

        /*
        int playerPosLane = myCar.position.lane;
        int playerPosBlock = myCar.position.block;

        int opponentPosLane = myCar.position.lane;
        int opponentPosBlock = myCar.position.block;
        */

        // Declaring player car status
        int currentSpeed = myCar.speed;
        int currentDamage = myCar.damage;

        //Fix first if too damaged to move
        if(currentDamage >= 3) {
            return FIX;
        }
        
        // If somehow car got stuck and does nothing, accelerate so it moves
        if (myCar.state == State.NOTHING) {
            return ACCELERATE;
        }
        
        //Basic avoidance logic
        if (checkLaneClearance(myCar)) {
            Command ordersLane = findClearLane(myCar);
            return ordersLane;
        } else {
            if (hasPowerUp(PowerUps.BOOST, myCar.powerups)) {
                return BOOST;
            }
        } 

        // Tries to find the nearest power ups
        
        if (hasAllPowerUps(myCar.powerups) == false) {
            Command ordersPower = findPowerUps(myCar);
            return ordersPower;
        }

        // Aggresive algorithm
        // Tweet usage
        if (hasPowerUp(PowerUps.TWEET, myCar.powerups)) {
            Command tweetUsage = useTweet(myCar, opponentCar);
            return tweetUsage;
        }

        // Emp usage
        if ((isPlayerinFront(myCar, opponentCar) == false) && (hasPowerUp(PowerUps.EMP, myCar.powerups))) {
            Command empUsage = useEMP(myCar, opponentCar);
            return empUsage;
        }
        
        //Accelerate first if going to slow
        if(myCar.speed <= 3) {
            return ACCELERATE;
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

    private boolean hasAllPowerUps(PowerUps[] available) {
        int boost = 0;
        int emp = 0;
        int lizard = 0;
        int oil = 0;
        int tweet = 0;
        for (PowerUps powerUp: available) {
            if (powerUp.equals(PowerUps.BOOST)) {
                boost++;
            }
            if (powerUp.equals(PowerUps.EMP)) {
                emp++;
            }
            if (powerUp.equals(PowerUps.LIZARD)) {
                lizard++;
            }
            if (powerUp.equals(PowerUps.OIL)) {
                oil++;
            }
            if (powerUp.equals(PowerUps.TWEET)) {
                tweet++;
            }
        }
        if ((boost > 0) && (emp > 0) && (lizard > 0) && (oil > 0) && (tweet > 0)) {
            return true;
        } else {
            return false;
        }
    }

    /*
    private boolean checkPowerUpsAhead(Car myCar, GameState gameState) {
        int myPosLane = myCar.position.lane;
        int myPosBlock = myCar.position.block;
        List<Object> sameLane = getInfoinLaneBased(myPosLane, myPosBlock, 1);

        if ((sameLane.contains(Terrain.BOOST)) || (sameLane.contains(Terrain.EMP)) || (sameLane.contains(Terrain.LIZARD)) || (sameLane.contains(Terrain.OIL_POWER)) || (sameLane.contains(Terrain.TWEET))){
            return true;
        } else {
            return false;
        }
    } */

    private boolean isPlayerinFront(Car myCar, Car opponentCar) {
        int myPosBlock = myCar.position.block;
        int opponentPosBlock = opponentCar.position.block;
        int diff = myPosBlock - opponentPosBlock;

        if (diff > 0) {
            return true;
        } else {
            return false;
        }
    }

    private boolean checkLaneClearance(Car myCar) {
        int myPosLane = myCar.position.lane;
        int myPosBlock = myCar.position.block;
        List<Object> sameLane = getInfoinLaneBased(myPosLane, myPosBlock, 1);

        if ((sameLane.contains(Terrain.WALL)) || (sameLane.contains(Terrain.MUD)) || (sameLane.contains(Terrain.OIL_SPILL))) {
            return true;
        } else {
            return false;
        }
    }

    private Command findPowerUps(Car myCar) {
        int i;
        int myPosLane = myCar.position.lane;
        int myPosBlock = myCar.position.block;
        int currentSpeed = myCar.speed;

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
        int mySpeed = myCar.speed;
        int firstLeftObstacle = 0;
        int firstRightObstacle = 0;
        int firstSameObstacle = 0;
        
        List<Object> sameLane = new ArrayList<Object>();
        List<Object> rightLane = new ArrayList<Object>();
        List<Object> leftLane = new ArrayList<Object>();
        List<Object> sameCT = new ArrayList<Object>();
        List<Object> rightCT = new ArrayList<Object>();
        List<Object> leftCT = new ArrayList<Object>();
        if (myPosLane == 1) {
            sameLane = getInfoinLaneBased(myPosLane, myPosBlock, 1);
            rightLane = getInfoinLaneBased(myPosLane, myPosBlock, 2);
            sameCT = getCTinLaneBased(myPosLane, myPosBlock, 1);
            rightCT = getCTinLaneBased(myPosLane, myPosBlock, 2);
        } else if (myPosLane == 4) {
            leftLane = getInfoinLaneBased(myPosLane, myPosBlock, 0);
            sameLane = getInfoinLaneBased(myPosLane, myPosBlock, 1);
            leftCT = getCTinLaneBased(myPosLane, myPosBlock, 0);
            sameCT = getCTinLaneBased(myPosLane, myPosBlock, 1);
        } else {
            leftLane = getInfoinLaneBased(myPosLane, myPosBlock, 0);
            sameLane = getInfoinLaneBased(myPosLane, myPosBlock, 1);
            rightLane = getInfoinLaneBased(myPosLane, myPosBlock, 2);
            leftCT = getCTinLaneBased(myPosLane, myPosBlock, 0);
            sameCT = getCTinLaneBased(myPosLane, myPosBlock, 1);
            rightCT = getCTinLaneBased(myPosLane, myPosBlock, 2);
        }

        if (myPosLane == 1) {
            if ((rightLane.contains(Terrain.WALL)) || (rightLane.contains(Terrain.MUD)) || (rightLane.contains(Terrain.OIL_SPILL)) || (checkCyberTruck(rightCT))) {
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
            if ((leftLane.contains(Terrain.WALL)) || (leftLane.contains(Terrain.MUD)) || (leftLane.contains(Terrain.OIL_SPILL)) || (checkCyberTruck(leftCT))) {
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
                if ((leftLane.get(i) == Terrain.WALL) || (leftLane.get(i) == Terrain.MUD) || (leftLane.get(i) == Terrain.OIL_SPILL) || (checkCyberTruck(leftCT))) {
                    firstLeftObstacle = i;
                    break;
                }
            }

            for (i = 0;i < rightLane.size(); i++) {
                if ((rightLane.get(i) == Terrain.WALL) || (rightLane.get(i) == Terrain.MUD) || (rightLane.get(i) == Terrain.OIL_SPILL) || (checkCyberTruck(rightCT))) {
                    firstRightObstacle = i;
                    break;
                }
            }

            for (i = 0;i < sameLane.size(); i++) {
                if ((sameLane.get(i) == Terrain.WALL) || (sameLane.get(i) == Terrain.MUD) || (sameLane.get(i) == Terrain.OIL_SPILL) || checkCyberTruck(sameCT)) {
                    firstSameObstacle = i;
                    break;
                }
            }
            
            if ((firstSameObstacle > firstLeftObstacle) && (firstSameObstacle > firstRightObstacle) && (firstLeftObstacle != 0) && (firstRightObstacle != 0) && (mySpeed < maxSpeed)) {
                return ACCELERATE;
            } else if ((firstLeftObstacle == 0) || (firstRightObstacle != 0)) {
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

    
    private Command useTweet(Car myCar, Car opponentCar) {
        int opponentPosLane = opponentCar.position.lane;
        int opponentPosBlock = opponentCar.position.block;
        int opponentSpeed = opponentCar.speed;
        return new TweetCommand(opponentPosLane, opponentPosBlock + opponentSpeed + 1);
    } 

    private Command useEMP(Car myCar, Car opponentCar) {
        int myPosLane = myCar.position.lane;
        int opponentPosLane = opponentCar.position.lane;
        int laneDiff = myPosLane - opponentPosLane;
        // If opponentPosLane > myPosLane -> means player have to turn right to get closer
        if (opponentPosLane > myPosLane) {
            return TURN_RIGHT;
        // If opponentPosLane < myPosLane -> means player have to turn left to get closer
        } else if (opponentPosLane < myPosLane) {
            return TURN_LEFT;
        } else if ((laneDiff == 2) && (laneDiff == -2)) {
            return EMP;
        }
        return EMP;
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
    /**
     * Returns map of blocks and the objects in the for the current lanes, returns
     * the amount of blocks that can be traversed at max speed.
     **/
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
