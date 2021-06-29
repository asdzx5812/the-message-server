package org.foop.finalproject.theMessageServer.round;

import org.foop.finalproject.theMessageServer.Action;
import org.foop.finalproject.theMessageServer.GameCards.Counteract;
import org.foop.finalproject.theMessageServer.Player;
import org.foop.finalproject.theMessageServer.Round;
import org.foop.finalproject.theMessageServer.action.*;
import org.foop.finalproject.theMessageServer.enums.MessageType;

public class GameCardRound extends Round {
    Action currentAction;

    GameCardRound(Player creator, Round round){
        super(round);
        this.creator = creator;
        currentPlayer = creator;
        endPlayer = getPreviousPlayer();
        name = "Game Card Round";
    }

    @Override
    public void onRoundStart() {
        System.out.println("GameCardRound: onRoundStart start.");
        //messageService.broadcastRoundStartMessage(game);
        onTurnStart();
        System.out.println("GameCardRound: onRoundStart end.");
    }

    @Override
    public void onTurnStart() {
        System.out.println("GameCardRound: onTurnStart start.");
        //messageService.broadcastTurnStartMessage(game, currentPlayer);
        messageService.broadcastPlayerToSelectAction(game, currentPlayer, MessageType.BROADCAST_PLAYER_START_SELECTING_GAMECARD);
        System.out.println("GameCardRound: onTurnStart end.");
    }

    @Override
    public void onTurnProgressing(Action action) {
        System.out.println("GameCardRound: onTurnProgressing start.");
        currentAction = action;
        // When receive a action game card from currentPlayer
        // start a counteract round
        //若打出gamecard
        if(action instanceof GameCardAction){
            endPlayer = currentPlayer;
            //檢查是不是需要target
            if(((GameCardAction) action).needToChooseTarget()){
                System.out.println("需要選擇target 開始targetRound");
                childRound = new TargetSelectRound(currentPlayer, this, action);
                game.setRound(childRound);
            }
            else {
                //選完target or 一般gamecard 開啟識破round
                System.out.println("選完target");
                childRound = new CounteractRound(currentPlayer, this);
                game.setRound(childRound);
                game.placeGameCardActionOnBoard((GameCardAction)action);

            }
            childRound.onRoundStart();


        }//打pass
        else if(action instanceof PassAction){
            onTurnEnd();
        }
        else{
            System.out.println("In GameCardRound send not pass or gamecard!!");
        }
        System.out.println("GameCardRound: onTurnProgressing end.");
    }

    private boolean checkIfCounteract(Action action) {
        System.out.println("GameCardRound: checkIfCounteract");
        if( action.getCard() instanceof Counteract){
            return true;
        }
        return false;
    }

    @Override
    public void onTurnEnd() {
        System.out.println("GameCardRound: onTurnEnd start.");

        //這個takeActionOnBoard 移到doWhenLeaveChildRound
        //game.takeActionOnBoard();

        if(satisfyRoundEndCondition() || game.winnerAppears()){
            onRoundEnd();
            return;
        }
        if(currentAction instanceof PassAction){
            currentPlayer = getNextPlayer();
        }
        System.out.println("GameCardRound: onTurnEnd end.");
        onTurnStart();
    }

    public void doWhenLeaveChildRound() {
        System.out.println("GameCardRound: doWhenLeaveChild start.");

        if(childRound instanceof TargetSelectRound) {
            setChildRound(null);
            onTurnProgressing(currentAction);
            //onTurnEnd();
        }
        else if(childRound instanceof CounteractRound) {
            setChildRound(null);
            //被移到這裡
            game.takeActionOnBoard();
        }
        else if(childRound instanceof ProveRound){
            onTurnEnd();
        }
        else {
            System.out.println("In GameCardRound no this type round!!!!");
        }


        //onTurnEnd(); 這個移到takeActionOnBoard裡做掉了（因為要判prove）
        System.out.println("GameCardRound: doWhenLeaveChild end.");
    }

    @Override
    public void onRoundEnd() {
        System.out.println("GameCardRound: onRoundEnd start.");
        // round的層級做事
        // 暫時沒事
        // game的層級做事
        game.leaveRound();
        System.out.println("GameCardRound: onRoundEnd end.");
    }

    @Override
    public boolean satisfyRoundEndCondition() {
        return endPlayer == currentPlayer && currentAction instanceof PassAction;
    }

}
