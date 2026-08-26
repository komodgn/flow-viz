import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import flowviz.resources.Res
import flowviz.resources.pretendard_bold
import flowviz.resources.pretendard_regular
import org.jetbrains.compose.resources.Font

/**
 * 사이드바에 나열되는 씬 목록.
 *
 * @property label 사이드바에 표시할 짧은 이름
 * @property title 씬 헤더의 제목
 * @property subtitle 씬 헤더의 부제
 */
enum class Scene(val label: String, val title: String, val subtitle: String) {
    Cold("Cold Flow", "Cold Flow", "collect 해야 흐른다"),
    Hot("Hot / StateFlow", "Hot Flow — StateFlow", "이미 흐르는 강물, 최신값 1개를 보관"),
    Bug("The Bug", "The Bug — conflation hang", "같은 값은 emit 안 돼서 영영 기다린다"),
    Shared("SharedFlow", "SharedFlow — 일회성 이벤트", "replay=0, conflation 없음"),
    MapCombine("map / combine", "map · combine", "값을 변환하고 여러 스트림을 합치기"),
    StateIn("stateIn", "stateIn — Cold를 Hot으로", "upstream 1번만 돌리고 공유"),
}

/**
 * 앱 루트.
 *
 * 한글 폰트(Pretendard)를 앱 전역 기본 폰트로 지정하고, 왼쪽 [Sidebar]로
 * 선택한 [Scene]에 해당하는 화면을 렌더링한다.
 */
@Composable
fun App() {
    val appFont = FontFamily(
        Font(Res.font.pretendard_regular, FontWeight.Normal),
        Font(Res.font.pretendard_bold, FontWeight.Bold),
    )

    MaterialTheme(colorScheme = darkColorScheme(background = Bg)) {
        CompositionLocalProvider(
            LocalTextStyle provides LocalTextStyle.current.copy(fontFamily = appFont)
        ) {
            var selected by remember { mutableStateOf(Scene.Cold) }

            Row(Modifier.fillMaxSize().background(Bg)) {
                Sidebar(selected) { selected = it }
                Box(Modifier.fillMaxSize()) {
                    when (selected) {
                        Scene.Cold -> ColdFlowScene()
                        Scene.Hot -> HotFlowScene()
                        Scene.Bug -> BugScene()
                        Scene.Shared -> SharedFlowScene()
                        Scene.MapCombine -> MapCombineScene()
                        Scene.StateIn -> StateInScene()
                    }
                }
            }
        }
    }
}

/** 왼쪽 씬 네비게이션. [Scene.entries]를 순회해 항목을 그리고 선택을 [onSelect]로 알린다. */
@Composable
private fun Sidebar(selected: Scene, onSelect: (Scene) -> Unit) {
    Column(
        Modifier.width(200.dp).fillMaxHeight().background(SidebarBg).padding(20.dp)
    ) {
        Text("Flow Viz", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("Coroutines Flow 시각화", color = Muted, fontSize = 11.sp)
        Spacer(Modifier.height(28.dp))

        Scene.entries.forEach { scene ->
            val active = scene == selected
            Row(
                Modifier.fillMaxWidth()
                    .padding(vertical = 3.dp)
                    .background(if (active) Card else Color.Transparent, RoundedCornerShape(8.dp))
                    .clickable { onSelect(scene) }
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Text(
                    scene.label,
                    color = if (active) Color.White else Muted,
                    fontSize = 14.sp,
                    fontWeight = if (active) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}
