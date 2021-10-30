import React, { FunctionComponent, useState } from "react";
import styled from "styled-components";
import { useHistory } from "react-router-dom";
import { Button, TextField } from "@mui/material";

const InputContainter = styled.div`
  width: 100%;
  display: flex;

  justify-content: center;
  align-items: center;
  flex-direction: row;
`;

const FormContainer = styled.div`
  width: 500px;
  @media (max-width: 500px) {
    width: 100vw;
  }
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
`;

const PageContainer = styled.div`
  width: 100vw;
  min-height: 60vh; // may need to be >100vh on mobile
  display: flex;
  justify-content: center;
  align-items: center;
  flex-direction: column;
`;

export const Home: React.FunctionComponent<{}> = (props) => {
  const [roomName, setRoomName] = useState("");

  const history = useHistory();
  const goToRoom = () => {
    if (roomName.length > 0) {
      history.push(`${roomName}`);
    }
  };

  return (
    <>
      <PageContainer>
        <FormContainer>
          <h1>Cobra Fuel</h1>
          <p>A pitch selling game!</p>
          <InputContainter>
            <TextField
              id="outlined-basic"
              label="Room name"
              variant="outlined"
              size="medium"
              placeholder="Room name"
              fullWidth
              onChange={(e) => setRoomName(e.target.value)}
              onKeyPress={(e) => {
                if (e.key === "Enter") {
                  goToRoom();
                }
              }}
            />
            <Button variant="contained" onClick={goToRoom}>
              Join room
            </Button>
          </InputContainter>
        </FormContainer>
      </PageContainer>
    </>
  );
};
