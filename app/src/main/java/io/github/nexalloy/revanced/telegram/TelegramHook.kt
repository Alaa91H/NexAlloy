package io.github.nexalloy.revanced.telegram

import io.github.nexalloy.revanced.telegram.ads.RemoveSponsoredAds
import io.github.nexalloy.revanced.telegram.forward.ForwardOptions
import io.github.nexalloy.revanced.telegram.behavior.DisableMediaAutoplay
import io.github.nexalloy.revanced.telegram.behavior.OpenLinksExternally
import io.github.nexalloy.revanced.telegram.download.DownloadBoostBalanced
import io.github.nexalloy.revanced.telegram.download.DownloadBoostMaximum
import io.github.nexalloy.revanced.telegram.history.KeepDeletedMessages
import io.github.nexalloy.revanced.telegram.history.KeepOriginalEditedMessages
import io.github.nexalloy.revanced.telegram.notifications.DisableNotificationMarkRead
import io.github.nexalloy.revanced.telegram.notifications.KeepUnreadAfterQuickReply
import io.github.nexalloy.revanced.telegram.privacy.HideAllChatActivity
import io.github.nexalloy.revanced.telegram.privacy.HideContentReadReceipts
import io.github.nexalloy.revanced.telegram.privacy.HideEmojiInteractions
import io.github.nexalloy.revanced.telegram.privacy.HideGroupCallSpeakingStatus
import io.github.nexalloy.revanced.telegram.privacy.HideOnlineStatus
import io.github.nexalloy.revanced.telegram.privacy.HideReadReceipts
import io.github.nexalloy.revanced.telegram.privacy.HideRecordingAndUploadStatus
import io.github.nexalloy.revanced.telegram.privacy.HideScreenshotNotifications
import io.github.nexalloy.revanced.telegram.privacy.HideStoryViews
import io.github.nexalloy.revanced.telegram.privacy.HideTypingStatus
import io.github.nexalloy.revanced.telegram.privacy.GhostExceptions
import io.github.nexalloy.revanced.telegram.privacy.HideChoosingContactStatus
import io.github.nexalloy.revanced.telegram.privacy.HideChoosingLocationStatus
import io.github.nexalloy.revanced.telegram.privacy.HideChoosingStickerStatus
import io.github.nexalloy.revanced.telegram.privacy.HideEmojiAcknowledgementStatus
import io.github.nexalloy.revanced.telegram.privacy.HideFileUploadStatus
import io.github.nexalloy.revanced.telegram.privacy.HidePhotoUploadStatus
import io.github.nexalloy.revanced.telegram.privacy.HidePlayingGameStatus
import io.github.nexalloy.revanced.telegram.privacy.HideVideoRecordingStatus
import io.github.nexalloy.revanced.telegram.privacy.HideVideoUploadStatus
import io.github.nexalloy.revanced.telegram.privacy.HideVoiceRecordingStatus
import io.github.nexalloy.revanced.telegram.privacy.HideVoiceUploadStatus

val TelegramPatches = arrayOf(
    RemoveSponsoredAds,
    ForwardOptions,
    GhostExceptions,
    KeepDeletedMessages,
    KeepOriginalEditedMessages,
    DownloadBoostBalanced,
    DownloadBoostMaximum,
    DisableMediaAutoplay,
    OpenLinksExternally,
    DisableNotificationMarkRead,
    KeepUnreadAfterQuickReply,
    HideOnlineStatus,
    HideReadReceipts,
    HideContentReadReceipts,
    HideTypingStatus,
    HideRecordingAndUploadStatus,
    HideEmojiInteractions,
    HideGroupCallSpeakingStatus,
    HideStoryViews,
    HideScreenshotNotifications,
    HideVoiceRecordingStatus,
    HideVideoRecordingStatus,
    HideVoiceUploadStatus,
    HideVideoUploadStatus,
    HidePhotoUploadStatus,
    HideFileUploadStatus,
    HideChoosingLocationStatus,
    HideChoosingContactStatus,
    HideChoosingStickerStatus,
    HidePlayingGameStatus,
    HideEmojiAcknowledgementStatus,
    HideAllChatActivity,
)
