import { Button } from "@mui/material";
import React, { MouseEventHandler } from "react";
import styled from "styled-components";

const RowContainer = styled.div`
  width: 100vw;
  border: 2px red;
  border-radius: 10%;
  display: flex;
`;

const JudgeIndicator = styled.div`
  height: 25px;
  width: 25px;
  background-color: red;
  border-radius: 50%;
  display: inline-block;
`;

const PlayerRow = ({
  playerInfo,
  isJudge,
  judgeRole,
  showJudgeControls,
  onPlayerWin
}: {
  playerInfo: Player;
  isJudge: boolean;
  judgeRole: string | null;
  showJudgeControls: boolean;
  onPlayerWin: MouseEventHandler<HTMLButtonElement> | undefined;
}) => {
  return (
      <RowContainer>
        <h1>{playerInfo.name ?? "Player"}</h1>
        {isJudge && (
          <>
            <h3>Role: {judgeRole}</h3>
            <JudgeIndicator />
          </>
        )}
        <h4>Roles won: </h4>
        {playerInfo?.rolesWon?.map((role) => {
          return <p>{role}</p>;
        })}

      {!isJudge && showJudgeControls && <Button variant="contained" color="success" onClick={onPlayerWin}>Select</Button>}
      </RowContainer>
  );
};

export default PlayerRow;
