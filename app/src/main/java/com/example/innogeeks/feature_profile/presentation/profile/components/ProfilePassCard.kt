package com.example.innogeeks.feature_profile.presentation.profile.components

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.innogeeks.core.presentation.components.DashedDivider
import com.example.innogeeks.core.presentation.components.passCardSurface
import com.example.innogeeks.ui.theme.InnogeeksTheme
import edu.kiet.innogeeks.R

// Profile's twin of the Home pass: identity, role, and the admin-assigned domain with its mascot.
@Composable
fun ProfilePassCard(
    initials: String,
    name: String,
    email: String,
    roleLabel: String,
    domainName: String?,
    @DrawableRes mascot: Int?,
    modifier: Modifier = Modifier
) {
    val primary = MaterialTheme.colorScheme.primary

    Box(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .passCardSurface()
                .padding(start = 18.dp, end = 18.dp, top = 18.dp, bottom = 14.dp)
        ) {
            Text(
                text = stringResource(R.string.pass_card_club_label),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.sp,
                color = primary.copy(alpha = 0.85f)
            )
            Row(
                modifier = Modifier.padding(top = 14.dp, end = if (mascot != null) 110.dp else 0.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier.size(52.dp).clip(CircleShape).background(primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Column {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = email,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            DashedDivider(modifier = Modifier.padding(top = 18.dp, bottom = 12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PassCell(
                    label = stringResource(R.string.profile_role),
                    value = roleLabel,
                    modifier = Modifier.weight(1f)
                )
                PassCell(
                    label = stringResource(R.string.profile_domain),
                    value = domainName ?: stringResource(R.string.profile_domain_not_assigned),
                    isLocked = domainName != null,
                    isMuted = domainName == null,
                    modifier = Modifier.weight(1.6f)
                )
            }

            if (domainName != null) {
                Text(
                    text = stringResource(R.string.profile_domain_assigned_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }
        }

        if (mascot != null) {
            Image(
                painter = painterResource(mascot),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 4.dp, y = (-58).dp)
                    .size(150.dp)
            )
        }
    }
}

@Composable
private fun PassCell(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    isLocked: Boolean = false,
    isMuted: Boolean = false
) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (isLocked) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = if (isMuted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, heightDp = 340)
@Composable
private fun ProfilePassCardMemberPreview() {
    InnogeeksTheme {
        ProfilePassCard(
            initials = "TM", name = "Test Member", email = "test.member@kiet.edu",
            roleLabel = "Member", domainName = "App Dev", mascot = R.drawable.ic_domain_appd,
            modifier = Modifier.padding(start = 18.dp, end = 18.dp, top = 70.dp)
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, heightDp = 340)
@Composable
private fun ProfilePassCardCoordinatorLongNamePreview() {
    InnogeeksTheme {
        ProfilePassCard(
            initials = "AK", name = "Atul Kumar Singh", email = "atul.kumar.singh@kiet.edu",
            roleLabel = "Coordinator", domainName = "Machine Learning", mascot = R.drawable.ic_domain_ml,
            modifier = Modifier.padding(start = 18.dp, end = 18.dp, top = 70.dp)
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, heightDp = 340)
@Composable
private fun ProfilePassCardAdminPreview() {
    InnogeeksTheme {
        ProfilePassCard(
            initials = "RS", name = "Riya Sharma", email = "riya.sharma@kiet.edu",
            roleLabel = "Admin", domainName = "Web Dev", mascot = R.drawable.ic_domain_webd,
            modifier = Modifier.padding(start = 18.dp, end = 18.dp, top = 70.dp)
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, heightDp = 260)
@Composable
private fun ProfilePassCardRegisteredPreview() {
    InnogeeksTheme {
        ProfilePassCard(
            initials = "AK", name = "Ayush Kumar", email = "ayush.kumar@kiet.edu",
            roleLabel = "Registered", domainName = null, mascot = null,
            modifier = Modifier.padding(18.dp)
        )
    }
}
