import React from 'react'
import styled from 'styled-components'

const RowContainer = styled.div`
    width: 100vw;
    border: 2px red;
    border-radius: 10%;
    display: flex;
`

const IsJudge = styled.div`
  height: 25px;
  width: 25px;
  background-color: red;
  border-radius: 50%;
  display: inline-block;
`


const PlayerRow = ({playerInfo, isJudge, judgeRole} : {playerInfo: Player, isJudge: boolean, judgeRole: String | null}) => {
    return (
        <>
        <RowContainer>
            <h1>{playerInfo.name}</h1>
            {isJudge && <><h3>Role: {judgeRole}</h3><IsJudge></IsJudge></>}
            <h4>Roles won: </h4>
            {playerInfo.rolesWon.map((role) => {
                return(
                <p>{role}</p>
                )
            })}
        </RowContainer>
        </>
    )
}

export default PlayerRow
