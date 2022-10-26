package com.pintertamas.befake.network.model

import com.pintertamas.befake.domain.util.DomainMapper
import com.pintertamas.befake.domain.model.Jwt

class JwtDtoMapper : DomainMapper<JwtDto, Jwt> {
    override fun mapToDomainModel(entity: JwtDto): Jwt {
        return Jwt(
            userId = entity.userId,
            username = entity.username,
            email = entity.email,
            jwtToken = entity.jwtToken
        )
    }

    override fun mapFromDomainModel(domainModel: Jwt): JwtDto {
        return JwtDto(
            userId = domainModel.userId,
            username = domainModel.username,
            email = domainModel.email,
            jwtToken = domainModel.jwtToken
        )
    }
}