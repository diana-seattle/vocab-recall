package org.indiv.dls.games.vocabrecall.feature

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.widget.TextView

/**
 * Styled [TextView] used in puzzle.
 */
open class PuzzleCellTextView @JvmOverloads constructor(context: Context,
                                                        attrs: AttributeSet? = null,
                                                        defStyleAttr: Int = 0)
    : TextView(context, attrs, defStyleAttr) {

    //region INITIALIZER ---------------------------------------------------------------------------

    init {
        val size = Math.round(resources.getDimension(R.dimen.cell_width))
        val fontHeight = size * FONT_SIZE_FRACTION
        gravity = Gravity.CENTER
        // need to create Drawable object for each TextView
        background = resources.getDrawable(R.drawable.cell_drawable)  // non-deprecated method not supported until API level 21
        setTextSize(TypedValue.COMPLEX_UNIT_PX, fontHeight)
        width = size
        height = size
        background.level = CELL_BKGD_LEVEL_NORMAL // default to normal text cell background (i.e. no error indication)
        //		setSoundEffectsEnabled(false); // true by default, consider disabling since we're providing our own vibration (except not all devices have vibration)
    }

    //endregion

    //region COMPANION OBJECT ----------------------------------------------------------------------

    companion object {
        private val FONT_SIZE_FRACTION = .74f
        private val TENTATIVE_COLOR = -0x555556 // Color.LTGRAY is 0xFFCCCCCC, Color.GRAY is 0xFF888888
        private val CONFIDENT_COLOR = Color.BLACK // Color.LTGRAY is 0xFFCCCCCC, Color.GRAY is 0xFF888888

        private val CELL_BKGD_LEVEL_NORMAL = 1
        private val CELL_BKGD_LEVEL_ERRORED = 2
        private val CELL_BKGD_LEVEL_SELECTED = 3
        private val CELL_BKGD_LEVEL_ERRORED_SELECTED = 4
        private val ERRORED_BACKGROUND_LEVLS = listOf(CELL_BKGD_LEVEL_ERRORED, CELL_BKGD_LEVEL_ERRORED_SELECTED)
    }

    //endregion

    //region PUBLIC FUNCTIONS ----------------------------------------------------------------------

    /**
     * Fills [TextView] with the character from the user's answer.
     *
     * @param userChar the character to fill the textview with.
     * @param confident true if confident color should be used, false otherwise.
     */
    fun fillTextView(userChar: Char?, confident: Boolean) {
        text = userChar?.toString()
        setTextColor(if (confident) CONFIDENT_COLOR else TENTATIVE_COLOR)
    }


    fun setTextColorConfidence(confident: Boolean) {
        setTextColor(if (confident) CONFIDENT_COLOR else TENTATIVE_COLOR)
    }

    /**
     * Sets background and text colors according to specified state.
     */
    fun setStyle(isConfident: Boolean, isSelected: Boolean, indicateError: Boolean) {
        if (indicateError) {
            background.level = if (isSelected) CELL_BKGD_LEVEL_ERRORED_SELECTED else CELL_BKGD_LEVEL_ERRORED
            setTextColor(Color.RED)
        } else {
            background.level = if (isSelected) CELL_BKGD_LEVEL_SELECTED else CELL_BKGD_LEVEL_NORMAL // set normal text cell background
            setTextColor(if (isConfident) CONFIDENT_COLOR else TENTATIVE_COLOR)
        }
    }

    /**
     * Updates cell background to selected or unselected while maintaining existing style (e.g. error state).
     */
    fun setSelection(selected: Boolean) {
        val showingError = background.level in ERRORED_BACKGROUND_LEVLS
        background.level = when {
            selected && showingError -> CELL_BKGD_LEVEL_ERRORED_SELECTED
            selected && !showingError -> CELL_BKGD_LEVEL_SELECTED
            !selected && showingError -> CELL_BKGD_LEVEL_ERRORED
            else -> CELL_BKGD_LEVEL_NORMAL
        }
    }

    /**
     * Updates size of the text view.
     */
    fun updateSize(size: Int) {
        val fontHeight = size * FONT_SIZE_FRACTION
        setTextSize(TypedValue.COMPLEX_UNIT_PX, fontHeight)
        width = size
        height = size
    }

    //endregion
}

/**
 * Styled [TextView] used in puzzle representation.
 */
class PuzzleRepresentationCellTextView @JvmOverloads constructor(context: Context,
                                                                 attrs: AttributeSet? = null,
                                                                 defStyleAttr: Int = 0)
    : PuzzleCellTextView(context, attrs, defStyleAttr) {

    //region INITIALIZER ---------------------------------------------------------------------------

    init {
        val size = Math.round(resources.getDimension(R.dimen.cell_representation_width))
        updateSize(size)
    }

    //endregion
}