package com.arcora.data.repository

import com.arcora.data.api.ArcOraApi
import com.arcora.data.api.ProfileImageRequest
import com.arcora.data.api.mapApiErrors
import com.arcora.domain.repository.ProfileRepository
import com.arcora.domain.repository.UserProfile
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiProfileRepository @Inject constructor(
    private val api: ArcOraApi
) : ProfileRepository {
    override suspend fun getProfile(): UserProfile = mapApiErrors {
        val r = api.me()
        UserProfile(
            id = r.id,
            email = r.email,
            username = r.username,
            displayName = r.displayName,
            smartAccountAddress = r.smartAccountAddress,
            reputationScore = r.reputationScore,
            isVerified = r.isVerified,
            profileImageUrl = r.profileImageUrl
        )
    }

    override suspend fun updateProfileImage(imageBase64: String, mimeType: String): String? = mapApiErrors {
        api.uploadProfileImage(ProfileImageRequest(imageBase64, mimeType)).profileImageUrl
    }
}
