import { Button, Tooltip } from "@mui/material"
import React, { MouseEventHandler } from "react"
import styled from "styled-components"

const RowContainer = styled.div`
  width: 100vw;
  border: 2px red;
  border-radius: 10%;
  display: flex;
`

const JudgeIndicator = styled.div`
  height: 25px;
  width: 25px;
  background-color: red;
  border-radius: 50%;
  display: inline-block;
`

const RolesWonContainer = styled.div`
  background-color: #f51515;
  color: #82df82;
  display: block;
  height: auto;
`

const PlayerRow = ({
  playerInfo,
  isJudge,
  judgeRole,
  showJudgeControls,
  onPlayerWin,
}: {
  playerInfo: Player
  isJudge: boolean
  judgeRole: string | null
  showJudgeControls: boolean
  onPlayerWin: MouseEventHandler<HTMLButtonElement> | undefined
}) => {
  return (
    <RowContainer>
      <span>{playerInfo.name ?? "Player"}</span>
      {isJudge && (
        <>
          <span>Role: {judgeRole}</span>
          <JudgeIndicator />
        </>
      )}
      <Tooltip title={playerInfo?.rolesWon?.join(", ")}>
        <RolesWonContainer>Roles won: {playerInfo.rolesWon?.length}</RolesWonContainer>
        {/* {playerInfo?.rolesWon?.map((role) => {
          return <p>{role}</p>;
        })} */}
      </Tooltip>

      {!isJudge && showJudgeControls && (
        <Button variant="contained" color="success" onClick={onPlayerWin}>
          Select
        </Button>
      )}
    </RowContainer>
  )
}

export default PlayerRow
