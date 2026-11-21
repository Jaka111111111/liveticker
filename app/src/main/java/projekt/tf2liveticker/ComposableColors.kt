package projekt.tf2liveticker

import androidx.compose.ui.graphics.Color

object ComposableColors {
    val buttonUnclicked: Color = Color(0xff272f36) //hässlich
    val buttonClicked: Color = Color(0xff3c4954)
    val backdrop: Color = Color(0xff131a1f)
    val highlightText: Color = Color(0xffe68624)
    val regularText: Color = Color.White
    val itemFill: Color = Color(0xff364049)

    fun getButtonColor(clicked: Boolean): Color {
        return if (clicked) {
            buttonClicked
        } else {
            buttonUnclicked
        }
    }
}