package io.github.nexalloy.revanced.telegram

import io.github.nexalloy.revanced.telegram.ads.RemoveSponsoredAds
import io.github.nexalloy.revanced.telegram.forward.ForwardOptions
import io.github.nexalloy.revanced.telegram.privacy.HideAllChatActivity
import io.github.nexalloy.revanced.telegram.privacy.HideContentReadReceipts
import io.github.nexalloy.revanced.telegram.privacy.HideEmojiInteractions
import io.github.nexalloy.revanced.telegram.privacy.HideGroupCallSpeakingStatus
import io.github.nexalloy.revanced.telegram.privacy.HideReadReceipts
import io.github.nexalloy.revanced.telegram.privacy.HideRecordingAndUploadStatus
import io.github.nexalloy.revanced.telegram.privacy.HideScreenshotNotifications
import io.github.nexalloy.revanced.telegram.privacy.HideStoryViews
import io.github.nexalloy.revanced.telegram.privacy.HideTypingStatus

val TelegramPatches = arrayOf(
    RemoveSponsoredAds,
    ForwardOptions,
    HideReadReceipts,
    HideContentReadReceipts,
    HideTypingStatus,
    HideRecordingAndUploadStatus,
    HideEmojiInteractions,
    HideGroupCallSpeakingStatus,
    HideStoryViews,
    HideScreenshotNotifications,
    HideAllChatActivity,
)
