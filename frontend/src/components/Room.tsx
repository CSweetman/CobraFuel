import { Dialog, DialogTitle, LinearProgress } from "@mui/material";
import React, {useEffect, useRef, useState} from "react";
import {useParams} from "react-router";
import useWebSocket, {ReadyState} from "react-use-websocket";
import styled from "styled-components";
import PlayerRow from "./PlayerRow";

type codeType = {
  roomCode: string;
};
export const Room: React.FunctionComponent<{}> = (props) => {
    // @ts-ignore  -- tell typescript to stfu
    const {roomCode} = useParams()
    const {
        sendMessage,
        sendJsonMessage,
        lastMessage,
        lastJsonMessage,
        readyState,
    } = useWebSocket(`ws://127.0.0.1/${roomCode}`, { // TODO: Change in production
        retryOnError: true,
        reconnectInterval: 4000,
        reconnectAttempts: 9
    })
    const connectionStatus = {
        [ReadyState.CONNECTING]: 'Connecting to server...',
        [ReadyState.OPEN]: 'Connection established.',
        [ReadyState.CLOSING]: 'Closing...',
        [ReadyState.CLOSED]: 'Connection closed.',
        [ReadyState.UNINSTANTIATED]: 'Connection uninstantiated',
    }[readyState];

    const [myPlayerID, setMyPlayerID] = useState<number>(-1)
    const [players, setPlayers] = useState<Array<Player>>([])
    const [judgePlayerID, setJudgePlayerID] = useState<number>(-1) 
    const [judgeRole, setJudgeRole] = useState<string>("")
    const [playerHand, setPlayerHand] = useState<Array<string>>([])

    // When we become judge, these are the roles we can pick from.
    // The role picker dialog is open when this is not null.
    const [roleCards, setRoleCards] = useState<Array<string> | null>(null)

    useEffect(() => {
        if (lastJsonMessage) {
            const message = lastJsonMessage as Message
            
            if (message.initializeClient) { // Initial state (first messsage) from server, only received once
                setMyPlayerID(message.initializeClient.playerID)
                const roomData = message.initializeClient.roomData
                setPlayers(roomData.playerList)
                setJudgePlayerID(roomData.judgePlayerID)
                setJudgeRole(roomData.judgeRole)
                setPlayerHand(message.initializeClient.startingHand)
            } else if (message.playerJoined) { // when another player joins after this client
                setPlayers([...players, message.playerJoined.newPlayer])
            } else if (message.playerLeft){
                setPlayers(players.filter(player=> player.id != message.playerLeft?.playerID))
            } else if (message.setName){
                const newPlayers : Array<Player> = {...players}
                const player = newPlayers.find(player => player.id === message.setName?.playerID)
                player!!.name = message.setName.name
                setPlayers(newPlayers) // tell React players has change
            } else if (message.selectionOfRole){
                setJudgeRole(message.selectionOfRole.role)
            } else if (message.endOfRound) {
                setPlayerHand([...playerHand, ...message.endOfRound.newCards])
                setJudgePlayerID(message.endOfRound.newJudgeID)
               

                if (message.endOfRound.winnerPlayerID) {
                    const newPlayers = [...players]
                    const winningPlayer = newPlayers.find(player => player.id === message.endOfRound!!.winnerPlayerID)!!
                    if (!winningPlayer.rolesWon) 
                      winningPlayer.rolesWon = []
                    winningPlayer.rolesWon = [...winningPlayer.rolesWon, judgeRole] // they convinced this judge so they win the title
                    setPlayers(newPlayers)
                    // TODO: Show toast/notification of player winning round
                }

                setJudgeRole("") // judge needs to pick which role they want

                if (message.endOfRound.roleCards) {
                    // we are the new judge and need to pick which role we want
                    setRoleCards(message.endOfRound.roleCards)
                }
            }
        }
    }, [lastJsonMessage])



    return (
      <>
        <Container>
          {players.map(player => <PlayerRow 
            playerInfo={player}
            isJudge={player.id === judgePlayerID}
            judgeRole={judgeRole}
            showJudgeControls={myPlayerID === judgePlayerID}
            onPlayerWin = {() => {
              const message: Message = {
                ...blankMessage,
                endOfRoundRequest: {
                  winnerPlayerID: player.id
                },
              };
              sendJsonMessage(message)
            }}
          />)}
        </Container>

        <Dialog disableEscapeKeyDown open={roleCards != null}>
          {roleCards?.map((role) => (
            <Role
              onClick={() => {
                const message: Message = {
                  ...blankMessage,
                  selectionOfRole: {
                    role: role,
                  },
                };
                sendJsonMessage(message);
                setRoleCards(null); // close dialog
                
                // To reduce network traffic, the server skips the sender when broadcasting the selection
                // of the judge role, so we update the local judge role ourselves below.
                setJudgeRole(role);
              }}
            >
              {role}
            </Role>
          ))}
          <DialogTitle>Pick a role!</DialogTitle>
        </Dialog>

        <Dialog disableEscapeKeyDown open={readyState !== ReadyState.OPEN}>
          <DialogTitle>{connectionStatus}</DialogTitle>
          {(readyState === ReadyState.CONNECTING ||
            readyState === ReadyState.CLOSING) && <LinearProgress />}
        </Dialog>
      </>
    );
}

const Container = styled.div`
    display: flex;
    flex-direction: column;
    width: 100vw;
    height: 100vh;
`;


const Role = styled.div`
    border-radius:50;
    border: 1px solid red;
    padding: 0.5em;
    margin: 1em;
    background-color: pink;
`;

const blankMessage: Message = {
    cardsPlayed: undefined,
    endOfRound: undefined,
    endOfRoundRequest: undefined,
    initializeClient: undefined,
    playerJoined: undefined,
    playerLeft: undefined,
    selectionOfRole: undefined,
    setName: undefined
}