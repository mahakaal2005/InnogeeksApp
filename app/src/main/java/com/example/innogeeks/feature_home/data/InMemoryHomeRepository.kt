package com.example.innogeeks.feature_home.data

import edu.kiet.innogeeks.R
import com.example.innogeeks.feature_home.domain.HomeRepository
import com.example.innogeeks.feature_home.domain.model.Achievement
import com.example.innogeeks.feature_home.domain.model.ClubStats
import com.example.innogeeks.feature_home.domain.model.CultureMoment
import com.example.innogeeks.feature_home.domain.model.DomainPreview

// Guest-mode data source. The backend has no public endpoints, so guest content is local.
class InMemoryHomeRepository : HomeRepository {

    override suspend fun getClubStats(): Result<ClubStats> {
        return Result.success(
            ClubStats(
                totalMembers = 150,
                totalProjects = 45,
                totalDomains = 5,
                totalEvents = 24
            )
        )
    }

    override suspend fun getDomains(): Result<List<DomainPreview>> {
        return Result.success(
            listOf(
                DomainPreview(
                    id = "appd",
                    name = "App Dev",
                    wheelLabel = "APP D",
                    blurb = "Native & cross-platform builders shipping Android and iOS apps."
                ),
                DomainPreview(
                    id = "webd",
                    name = "Web Dev",
                    wheelLabel = "WEB D",
                    blurb = "Full-stack crews building the club's own platforms."
                ),
                DomainPreview(
                    id = "ml",
                    name = "Machine Learning",
                    wheelLabel = "ML",
                    blurb = "Model-training practitioners chasing leaderboard ranks."
                ),
                DomainPreview(
                    id = "arvr",
                    name = "AR / VR",
                    wheelLabel = "AR VR",
                    blurb = "Immersive tinkerers building spatial experiences with Unity & WebXR."
                ),
                DomainPreview(
                    id = "iot",
                    name = "IoT",
                    wheelLabel = "IOT",
                    blurb = "Hardware and firmware hackers wiring sensors to the real world."
                )
            )
        )
    }

    override suspend fun getAchievements(): Result<List<Achievement>> {
        return Result.success(
            listOf(
                Achievement("a1", "🏆", "Finalist", "Smart India Hackathon"),
                Achievement("a2", "🚀", "Nominee", "NASA Space Apps — Global"),
                Achievement("a3", "🥈", "Top 50", "Flipkart GRiD 5.0"),
                Achievement("a4", "🛠️", "50+", "Projects Shipped"),
                Achievement("a5", "🎓", "40+", "Mentees Guided")
            )
        )
    }

    override suspend fun getTickerKeywords(): Result<List<List<String>>> {
        return Result.success(
            listOf(
                listOf("Technology", "Design", "Robotics", "Open Source"),
                listOf("Innovation", "Community", "Mentorship", "Research"),
                listOf("Hackathon", "Code", "Workshops", "Prototypes")
            )
        )
    }

    override suspend fun getCultureMoments(): Result<List<CultureMoment>> {
        // Newest first — reuses real event photos from feature_events' InMemoryEventsRepository.
        return Result.success(
            listOf(
                CultureMoment(
                    id = "cm1",
                    title = "NASA Space Apps Challenge",
                    caption = "Ghaziabad Edition · Sep 2025",
                    description = "150+ innovators, 35+ teams, and a ₹75,000 prize pool at KIET's own edition of NASA's global hackathon.",
                    imageRes = R.drawable.event_nasa_a,
                    eventId = "e27"
                ),
                CultureMoment(
                    id = "cm2",
                    title = "InnoForge",
                    caption = "Jun 2025",
                    description = "A hands-on build sprint where teams shipped working prototypes in a single day.",
                    imageRes = R.drawable.event_innoforge_a,
                    eventId = "e26"
                ),
                CultureMoment(
                    id = "cm3",
                    title = "Speaker Session",
                    caption = "Winter of Code 3.0 · Feb 2025",
                    description = "An industry speaker walked IWOC 2024–25 contributors through real-world open-source workflows.",
                    imageRes = R.drawable.event_iwoc3_speaker,
                    eventId = "e25"
                ),
                CultureMoment(
                    id = "cm4",
                    title = "Git & GitHub Session",
                    caption = "Version control basics · Feb 2025",
                    description = "Branching, committing, and collaborating on GitHub — covered end to end for first-timers.",
                    imageRes = R.drawable.event_git_feb2025_b_a,
                    eventId = "e28"
                ),
                CultureMoment(
                    id = "cm5",
                    title = "Winter of Code 3.0",
                    caption = "Open source · 2024–25",
                    description = "Innogeeks' flagship open-source program — students land their first real contributions.",
                    imageRes = R.drawable.event_iwoc3,
                    eventId = "e23"
                ),
                CultureMoment(
                    id = "cm6",
                    title = "Oraichain Season of Docs",
                    caption = "May 2024",
                    description = "Contributors documented a Web3 data-oracle project as part of a global Season of Docs program.",
                    imageRes = R.drawable.event_oraichain,
                    eventId = "e21"
                ),
                CultureMoment(
                    id = "cm7",
                    title = "Speaker Session",
                    caption = "Winter of Code 2.0 · Feb 2024",
                    description = "A guest speaker shared their journey into open source with IWOC 2.0 participants.",
                    imageRes = R.drawable.event_iwoc2_speaker,
                    eventId = "e18"
                ),
                CultureMoment(
                    id = "cm8",
                    title = "CoderSpree 3.0",
                    caption = "Competitive coding · Oct 2023",
                    description = "A campus-wide competitive programming arena open to every language and every year.",
                    imageRes = R.drawable.event_coderspree3,
                    eventId = "e15"
                ),
                CultureMoment(
                    id = "cm9",
                    title = "InnoHacks 2.0",
                    caption = "Hack & Innovate · Apr 2023",
                    description = "Teams built and pitched full products over one high-energy hackathon weekend.",
                    imageRes = R.drawable.event_innohacks2,
                    eventId = "e12"
                ),
                CultureMoment(
                    id = "cm10",
                    title = "Winter of Code 1.0",
                    caption = "Open source · Feb 2023",
                    description = "The first run of Innogeeks' open-source program, pairing beginners with real maintainers.",
                    imageRes = R.drawable.event_iwoc1,
                    eventId = "e11"
                ),
                CultureMoment(
                    id = "cm11",
                    title = "InnoHacks",
                    caption = "Hack N' Innovate · May 2022",
                    description = "The original InnoHacks — where the club's hackathon tradition started.",
                    imageRes = R.drawable.event_innohacks_2022,
                    eventId = "e8"
                ),
                CultureMoment(
                    id = "cm12",
                    title = "CoderSpree 1.0",
                    caption = "1000+ submissions · Oct 2021",
                    description = "The first CoderSpree drew 100+ contributors across C++, Java, and Python.",
                    imageRes = R.drawable.event_coderspree1,
                    eventId = "e3"
                )
            )
        )
    }
}
