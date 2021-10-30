import { Dialog, DialogTitle, LinearProgress } from "@mui/material";
import React, {useEffect, useRef, useState} from "react";
import {useParams} from "react-router";
import useWebSocket, {ReadyState} from "react-use-websocket";
import styled from "styled-components";


export const Room: React.FunctionComponent<{}> = (props) => {
    const {roomCode} = useParams<{ roomCode: string }>()
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
   
    useEffect(() => {
        if (lastJsonMessage) {
            const message = lastJsonMessage as Message

        

        }
    }, [lastJsonMessage])


    return <>

        <Dialog disableEscapeKeyDown
                open={readyState !== ReadyState.OPEN}>

            <DialogTitle>{connectionStatus}</DialogTitle>
            {(readyState === ReadyState.CONNECTING || readyState === ReadyState.CLOSING) && <LinearProgress />}
        </Dialog>

    </>
}