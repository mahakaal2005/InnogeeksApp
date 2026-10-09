package com.example.innogeeks.feature_home.presentation.memberhome.components

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.innogeeks.core.domain.model.UserDomain
import com.example.innogeeks.core.domain.model.UserRole
import com.example.innogeeks.core.presentation.mapper.toUiText
import com.example.innogeeks.feature_home.presentation.memberhome.AttendanceOverviewUi
import com.example.innogeeks.feature_home.presentation.memberhome.HomeSessionUi
import com.example.innogeeks.feature_home.presentation.memberhome.MarkStatus
import com.example.innogeeks.ui.theme.InnogeeksTheme
import edu.kiet.innogeeks.R

// The club ID card: domain mascot breaking out of the top edge, role pill, % and one mark per session.
@Composable
fun MemberPassCard(
    domain: UserDomain,
    role: UserRole,
    overview: AttendanceOverviewUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primary = MaterialTheme.colorScheme.primary
    val shape = RoundedCornerShape(26.dp)

    Box(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(Brush.linearGradient(passGradient()))
                .border(1.dp, primary.copy(alpha = 0.35f), shape)
                .clickable(onClick = onClick)
                .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 16.dp)
        ) {
            Text(
                text = stringResource(R.string.member_home_innogeeks),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.sp,
                color = primary.copy(alpha = 0.85f)
            )
            Text(
                text = domain.toUiText().asString(),
                style = MaterialTheme.typography.headlineLarge,
                fontSize = 38.sp,
                lineHeight = 40.sp,
                color = MaterialTheme.colorScheme.onSurface,
                // Keeps long names ("Machine Learning") out from under the mascot by wrapping them.
                modifier = Modifier.padding(top = 10.dp, end = 130.dp)
            )
            Text(
                text = role.pillLabel(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .padding(top = 10.dp)
                    .clip(RoundedCornerShape(percent = 50))
                    .background(primary)
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            )

            DashedDivider(modifier = Modifier.padding(top = 26.dp, bottom = 14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    if (overview.marked > 0) {
                        Text(
                            text = "${overview.percent}%",
                            style = MaterialTheme.typography.displayMedium,
                            fontSize = 44.sp,
                            color = primary
                        )
                        Text(
                            text = stringResource(R.string.member_home_sessions_attended, overview.present, overview.marked),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.member_home_no_score),
                            style = MaterialTheme.typography.titleLarge,
                            color = primary
                        )
                        Text(
                            text = stringResource(R.string.member_home_first_session_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.Bottom) {
                        overview.strip.forEach { MarkChip(it.status) }
                    }
                    if (overview.waiting > 0) {
                        Text(
                            text = stringResource(R.string.member_home_waiting_for_mark, overview.waiting),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }
            }
        }

        Image(
            painter = painterResource(domain.mascotRes()),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 6.dp, y = (-62).dp)
                .size(168.dp)
        )
    }
}

// A primary-tinted corner fading into the theme's own surface and background.
@Composable
private fun passGradient(): List<Color> {
    val scheme = MaterialTheme.colorScheme
    return listOf(scheme.primary.copy(alpha = 0.16f).compositeOver(scheme.surface), scheme.surfaceContainerLow, scheme.background)
}

@Composable
private fun DashedDivider(modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
    Spacer(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .drawBehind {
                drawLine(
                    color = color,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))
                )
            }
    )
}

@Composable
private fun UserRole.pillLabel(): String = stringResource(
    when (this) {
        UserRole.COORDINATOR -> R.string.member_home_role_coordinator
        UserRole.ADMIN -> R.string.member_home_role_admin
        else -> R.string.member_home_role_member
    }
)

internal fun UserDomain.mascotRes(): Int = when (this) {
    UserDomain.ANDROID -> R.drawable.ic_domain_appd
    UserDomain.WEB -> R.drawable.ic_domain_webd
    UserDomain.ML -> R.drawable.ic_domain_ml
    UserDomain.IOT -> R.drawable.ic_domain_iot
    UserDomain.AR_VR -> R.drawable.ic_domain_arvr
}

internal val previewSessions = listOf(
    HomeSessionUi("s6", "Ktor & REST APIs", "7 Oct", MarkStatus.UNMARKED),
    HomeSessionUi("s5", "Room + DataStore", "30 Sep", MarkStatus.MISSED),
    HomeSessionUi("s4", "Navigation in Compose", "23 Sep", MarkStatus.PRESENT),
    HomeSessionUi("s3", "State & recomposition", "16 Sep", MarkStatus.PRESENT),
    HomeSessionUi("s2", "Compose layouts", "9 Sep", MarkStatus.PRESENT),
    HomeSessionUi("s1", "Kotlin crash course", "2 Sep", MarkStatus.MISSED)
)

internal val previewOverview = AttendanceOverviewUi(percent = 60, present = 3, marked = 5, sessions = previewSessions)

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, heightDp = 360)
@Composable
private fun MemberPassCardPreview() {
    InnogeeksTheme {
        MemberPassCard(
            domain = UserDomain.ANDROID,
            role = UserRole.MEMBER,
            overview = previewOverview,
            onClick = {},
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 70.dp)
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, heightDp = 360)
@Composable
private fun MemberPassCardNoScorePreview() {
    InnogeeksTheme {
        MemberPassCard(
            domain = UserDomain.WEB,
            role = UserRole.COORDINATOR,
            overview = AttendanceOverviewUi(0, 0, 0, previewSessions.take(1)),
            onClick = {},
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 70.dp)
        )
    }
}
