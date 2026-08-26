import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow

/** wasmJs 진입점. index.html의 `#ComposeTarget` 캔버스에 Compose UI를 마운트한다. */
@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    CanvasBasedWindow(canvasElementId = "ComposeTarget") {
        App()
    }
}
