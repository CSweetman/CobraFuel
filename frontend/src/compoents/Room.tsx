import Dialog from '@mui/material/Dialog';
import DialogTitle from '@mui/material/DialogTitle';
import LinearProgress from '@mui/material/LinearProgress';
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
    } = useWebSocket(`wss://localhost:8080/${roomCode.toLowerCase()}`, {
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