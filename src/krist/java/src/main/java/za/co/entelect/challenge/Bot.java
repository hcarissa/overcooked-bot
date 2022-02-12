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
    private List<Integer> directionList = new ArrayList<>();

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
        Command res;
        List<Object> blocks = getBlocksInFront(myCar.position.lane, myCar.position.block);
        List<Object> nextBlocks = blocks.subList(0,1);
        Position player_pos = myCar.position;
        Position opp_pos = opponent.position;

        if (myCar.damage >= 5) {
            return FIX;
        }

        //Accelerate first if going too slow
        if(myCar.speed <= 3) {
            return ACCELERATE;
        }

        // Check opponent position
        if(opp_pos.block == myCar.position.block+1) {
            int i = random.nextInt(directionList.size());
            return new ChangeLaneCommand(directionList.get(i));
        }
        /* 

        if (isPlayerInFront(opp_pos, myCar.position) == 'front') {
            if (hasPowerUp(PowerUps.BOOST, myCar.powerups) || hasPowerUp(PowerUps.LIZARD, myCar.powerups) || hasPowerUp(PowerUps.OIL, myCar.powerups) || hasPowerUp(PowerUps.TWEET, myCar.powerups)) {
                if (hasPowerUp(PowerUps.BOOST, myCar.powerups)) {
                    return BOOST;
                } else if (hasPowerUp(PowerUps.LIZARD, myCar.powerups) || hasPowerUp(PowerUps.OIL, myCar.powerups) || hasPowerUp(PowerUps.TWEET, myCar.powerups)) {
                    if (hasPowerUp(PowerUps.TWEET, myCar.powerups) {
                        return TWEET();
                    }
                    if (hasPowerUp(PowerUps.LIZARD, myCar.powerups) {
                        return LIZARD;
                    }
                    if (hasPowerUp(PowerUps.OIL, myCar.powerups) {
                        return OIL;
                    }
                }
                return ACCELERATE;
            }   
        } else if (isPlayerInFront(opp_pos, myCar.position) == 'back') {
            if (hasPowerUp(PowerUps.BOOST, myCar.powerups)  || hasPowerUp(PowerUps.EMP, myCar.powerups) || hasPowerUp(PowerUps.OIL, myCar.powerups)) {
                if (hasPowerUp(PowerUps.BOOST, myCar.powerups)) {
                    return BOOST;
                } else if (hasPowerUp(PowerUps.OIL, myCar.powerups) || hasPowerUp(PowerUps.EMP, myCar.powerups)) {
                    if (hasPowerUp(PowerUps.EMP, myCar.powerups) && isEmptyInDistanceOpp(blocks)) {
                        return EMP;
                    }
                }
                return ACCELERATE;
            }
        } else {
            return ACCELERATE;
        }
        -> cari powerup terdekat untuk menuju situ

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
            -> avoid wall, oil_spill, mud
        */

        return ACCELERATE;
    }

    private Command avoid(List<Object> blocks, Position player_pos) {
        if (blocks.contains(Terrain.WALL) || blocks.contains(Terrain.OIL_SPILL) || blocks.contains(Terrain.MUD)) {
            if (player_pos.lane == 1) {
                return TURN_RIGHT;
            } else if (player_pos.lane == 4) {
                return TURN_LEFT;
            } else {
                List<Lane[]> map = gameState.lanes;
                int startBlock = map.get(0)[0].position.block;
                List<Object> leftBlocks = getBlocksInFrontLeft(player_pos.lane, player_pos.block);
                List<Object> rightBlocks = getBlocksInFrontRight(player_pos.lane, player_pos.block);
                int firstLeftObstacle = 0;
                int firstRightObstacle = 0;
                for (int i = max(player_pos.block - startBlock, 0); i < (leftBlocks.size() + player_pos.block); i++) {
                    if (blocks.get(i) == Terrain.WALL) {
                        firstLeftObstacle = i;
                        break;
                    }
                }

                for (int i = max(player_pos.block - startBlock, 0); i < (rightBlocks.size() + player_pos.block); i++) {
                    if (blocks.get(i) == Terrain.WALL) {
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

    private Boolean isEmptyInDistanceOpp(List<Object> blocks, Position player_pos) {
        List<Lane[]> map = gameState.lanes;
        int startBlock = map.get(0)[0].position.block;
        for (int i = max(player_pos.block - startBlock, 0); i < (blocks.size() + player_pos.block); i++) {
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

    private List<Object> getBlocksInFront(int lane, int block) {
        List<Lane[]> map = gameState.lanes;
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

    private List<Object> getBlocksInFrontRight(int lane, int block) {
        List<Lane[]> map = gameState.lanes;
        List<Object> blocks = new ArrayList<>();
        int startBlock = map.get(0)[0].position.block;

        Lane[] laneList = map.get(lane);
        for (int i = max(block - startBlock, 0); i <= block - startBlock + Bot.maxSpeed; i++) {
            if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
                break;
            }
            blocks.add(laneList[i].terrain);
        }
        return blocks;
    }

    private List<Object> getBlocksInFrontLeft(int lane, int block) {
        List<Lane[]> map = gameState.lanes;
        List<Object> blocks = new ArrayList<>();
        int startBlock = map.get(0)[0].position.block;

        Lane[] laneList = map.get(lane-2);
        for (int i = max(block - startBlock, 0); i <= block - startBlock + Bot.maxSpeed; i++) {
            if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
                break;
            }
            blocks.add(laneList[i].terrain);
        }
        return blocks;
    }

}
