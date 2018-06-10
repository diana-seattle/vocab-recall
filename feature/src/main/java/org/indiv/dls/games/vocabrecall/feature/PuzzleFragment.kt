package org.indiv.dls.games.vocabrecall.feature

import java.util.ArrayList

import org.indiv.dls.games.vocabrecall.feature.db.GameWord


import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.os.Vibrator
import android.support.v4.app.Fragment
import android.widget.Space
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.View.OnClickListener
import android.widget.TableRow
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_puzzle.*


/**
 * Fragment containing the crossword puzzle.
 */
class PuzzleFragment : Fragment() {

    //region COMPANION OBJECT ----------------------------------------------------------------------

    companion object {
        private val TAG = PuzzleFragment::class.java.simpleName
        private val TENTATIVE_COLOR = -0x555556 // Color.LTGRAY is 0xFFCCCCCC, Color.GRAY is 0xFF888888
        private val CONFIDENT_COLOR = Color.BLACK // Color.LTGRAY is 0xFFCCCCCC, Color.GRAY is 0xFF888888
        private val CELL_BKGD_LEVEL_NORMAL = 1
        private val CELL_BKGD_LEVEL_ERRORED = 2
        private val CELL_BKGD_LEVEL_SELECTED = 3
        private val CELL_BKGD_LEVEL_ERRORED_SELECTED = 4
    }

    //endregion

    //region PUBLIC INTERFACES ---------------------------------------------------------------------

    // interface for activity to implement to receive touch event
    interface PuzzleListener {
        fun onPuzzleClick(gameWord: GameWord?)
    }

    //endregion

    //region PRIVATE PROPERTIES --------------------------------------------------------------------

    private var gridWidth: Int = 0
    private var gridHeight: Int = 0
    private var pixelsPerCell: Int = 0
    private var vibrator: Vibrator? = null

    //endregion

    //region PUBLIC PROPERTIES ---------------------------------------------------------------------

    lateinit var cellGrid: Array<Array<GridCell?>>

    var currentGameWord: GameWord? = null
        private set(gameWord) {
            field?.let { showAsSelected(it, false) }
            gameWord?.let { showAsSelected(it, true) }
            field = gameWord
        }

    /*
     * implements interface for receiving callback from AnswerFragment
     */
    val puzzleRepresentation: List<TextView>
        get() {
            val views = ArrayList<TextView>()
            currentGameWord?.let {
                val r = resources
                val isAcross = it.isAcross
                val wordLength = it.word.length
                var row = it.row
                var col = it.col
                val size = Math.round(r.getDimension(R.dimen.cell_representation_width))
                val fontHeight = size * .75f
                for (charIndex in 0 until wordLength) {
                    val textView = createCellTextView(r, fontHeight, size)
                    views.add(textView)
                    cellGrid[row][col]?.let {
                        if (isAcross) {
                            if (it.gameWordDown != null) {
                                fillTextView(textView, it.userCharDown, it.gameWordDown!!.isConfident)
                            }
                            col++
                        } else {
                            if (it.gameWordAcross != null) {
                                fillTextView(textView, it.userCharAcross, it.gameWordAcross!!.isConfident)
                            }
                            row++
                        }
                    }
                }
            }
            return views
        }

    //endregion

    //region OVERRIDDEN FUNCTIONS ------------------------------------------------------------------

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        // inflate the view
        val view = inflater.inflate(R.layout.fragment_puzzle, container)

        // Get instance of Vibrator from current Context
        vibrator = activity!!.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        // Note that database not set up yet at this point (happening in other thread).
        // When it completes, it will call onFinishDbSetup().

        return view
    }

    //endregion

    //region PUBLIC FUNCTIONS ----------------------------------------------------------------------

    fun initialize(viewWidthPixels: Int, viewHeightPixels: Int) {

        val r = resources

        // calculate number of pixels equivalent to 24dp (24dp allows 13 cells on smallest screen supported by Android (320dp width, 426dp height))
        pixelsPerCell = Math.round(r.getDimension(R.dimen.cell_width))

        // set up grid
        gridHeight = viewHeightPixels / pixelsPerCell - 2  // subtract 2 because subtracting action bar height doesn't seem to be enough
        gridWidth = viewWidthPixels / pixelsPerCell - 1  // subtract 1 to give margin for consistency with height
        cellGrid = Array(gridHeight) { arrayOfNulls<GridCell>(gridWidth) }

        // create table rows
        for (row in 0 until gridHeight) {
            val tableRow = TableRow(activity)
            tableRow.gravity = Gravity.CENTER
            cell_table_layout.addView(tableRow)
        }
    }

    fun doWordsFitInGrid(gameWords: List<GameWord>): Boolean {
        gameWords.forEach {
            if ((it.row >= gridHeight || it.col >= gridWidth) ||
                    (it.isAcross && it.col + it.word.length > gridWidth) ||
                    (!it.isAcross && it.row + it.word.length > gridHeight)) {
                return false
            }
        }
        return true
    }

    /**
     * create new game (called first time app run, or when user starts new game)
     * implements interface for receiving callback from ConfirmStartNewGameDialogFragment
     */
    fun clearExistingGame() {
        // clear out any existing data
        currentGameWord = null
        for (row in 0 until gridHeight) {
            val tableRow = cell_table_layout.getChildAt(row) as TableRow
            tableRow.removeAllViews()
            cellGrid[row].fill(null)
        }
    }

    /**
     * Creates grid of textviews making up the puzzle.
     *
     * @param puzzleListener implementation of the [PuzzleListener] interface that listens for clicks on the puzzle.
     */
    fun createGrid(puzzleListener: PuzzleListener) {
        // create click listener for puzzle clicks
        val onPuzzleClickListener = OnClickListener { v ->
            getCellForView(v)?.let {
                vibrator?.vibrate(25)
                currentGameWord = it.gameWordDown ?: it.gameWordAcross
                puzzleListener.onPuzzleClick(currentGameWord)
            }
        }

        val res = resources
        val fontHeight = pixelsPerCell * .75f

        // add views into table rows and columns
        var firstGameWord: GameWord? = null
        for (row in 0 until gridHeight) {
            val tableRow = cell_table_layout.getChildAt(row) as TableRow
            tableRow.removeAllViews()
            for (col in 0 until gridWidth) {
                cellGrid[row][col]?.let {
                    // create text view for this row and column
                    val textView = createCellTextView(res, fontHeight, pixelsPerCell)
                    textView.setOnClickListener(onPuzzleClickListener)
                    tableRow.addView(textView, col)
                    it.view = textView

                    fillTextView(it)

                    // set current game word to the first across word found for sake of dual pane mode
                    if (firstGameWord == null) {
                        firstGameWord = it.gameWordAcross ?: it.gameWordDown
                    }
                } ?: run {
                    tableRow.addView(Space(activity), col)
                }
            }
        }
        currentGameWord = firstGameWord
    }

    /**
     * Selects next game word containing an error.
     */
    fun selectNextErroredGameWord(): Boolean {
        for (row in 0 until gridHeight) {
            for (col in 0 until gridWidth) {
                val gridCell = cellGrid[row][col]
                if (gridCell != null && gridCell.hasUserError()) {
                    currentGameWord = gridCell.gameWordAcross ?: gridCell.gameWordDown
                    return true
                }
            }
        }
        return false
    }

    /**
     * Indicates errored cells in the puzzle with a reddish background.
     *
     * @param showErrors true if errors should be indicated, false if not.
     */
    fun showErrors(showErrors: Boolean): Boolean {
        var numErrors = 0

        // update background of cells based on whether text is correct or not
        for (row in 0 until gridHeight) {
            for (col in 0 until gridWidth) {
                // if cell is part of currently selected game word, adjust the level to set the background color
                cellGrid[row][col]?.let {
                    val isSelected = currentGameWord == it.gameWordAcross || currentGameWord == it.gameWordDown
                    val textView = it.view
                    if (showErrors && it.hasUserError()) {
                        numErrors++
                        textView!!.background.level = if (isSelected) CELL_BKGD_LEVEL_ERRORED_SELECTED else CELL_BKGD_LEVEL_ERRORED // set error background
                        textView.setTextColor(Color.RED)
                    } else {
                        textView!!.background.level = if (isSelected) CELL_BKGD_LEVEL_SELECTED else CELL_BKGD_LEVEL_NORMAL // set normal text cell background
                        textView.setTextColor(if (it.isDominantCharConfident) CONFIDENT_COLOR else TENTATIVE_COLOR)
                    }
                }
            }
        }
        return numErrors > 0
    }

    /**
     * @param correctly true if puzzle considered complete when everything filled in correctly, false if puzzle
     * considered complete when everthing filled in regardless of correctness.
     * @return true if puzzle is completely filled in.
     */
    fun isPuzzleComplete(correctly: Boolean): Boolean {
        for (row in 0 until gridHeight) {
            for (col in 0 until gridWidth) {
                val gridCell = cellGrid[row][col]
                if (gridCell != null) {
                    // if cell is empty, then not complete
                    if (gridCell.dominantUserChar == null || correctly && gridCell.hasUserError()) {
                        return false
                    }
                }
            }
        }
        return true
    }

    /*
     * Fills in the puzzle with the user's answer for the specified game word.
     */
    fun updateUserTextInPuzzle(gameWord: GameWord) {
        // show answer in puzzle
        val userText = gameWord.userText
        val userTextLength = userText.length
        val wordLength = gameWord!!.word.length
        val isAcross = gameWord.isAcross
        var row = gameWord.row
        var col = gameWord.col
        for (charIndex in 0 until wordLength) {
            cellGrid[row][col]?.let {
                val userChar = if (charIndex < userTextLength) userText[charIndex] else null
                if (isAcross) {
                    it.userCharAcross = userChar
                    col++
                } else {
                    it.userCharDown = userChar
                    row++
                }
                fillTextView(it)
            }
        }
    }

    //endregion

    //region PRIVATE FUNCTIONS ---------------------------------------------------------------------

    /**
     * Shows the specified word as selected (yellow highlight).
     *
     * @param gameWord word to show as selected or unselected.
     * @param asSelected true if word should be shown as selected, false if unselected.
     */
    private fun showAsSelected(gameWord: GameWord?, asSelected: Boolean) {
        if (gameWord != null) {

//			int animationId = gameWord.isAcross()? R.anim.word_selection_horiz : R.anim.word_selection_vert;
//			Animation selectionAnimation = asSelected? AnimationUtils.loadAnimation(getActivity(), animationId) : null;

            var errorBackgroundLevels = listOf(CELL_BKGD_LEVEL_ERRORED, CELL_BKGD_LEVEL_ERRORED_SELECTED)
            var row = gameWord.row
            var col = gameWord.col
            for (i in 0 until gameWord.word.length) {
                val gridCell = cellGrid[row][col]
                gridCell?.view?.let {
                    val showingError = it.background.level in errorBackgroundLevels
                    it.background.level = when {
                        asSelected && showingError -> CELL_BKGD_LEVEL_ERRORED_SELECTED
                        !asSelected && showingError -> CELL_BKGD_LEVEL_ERRORED
                        asSelected && !showingError -> CELL_BKGD_LEVEL_SELECTED
                        else -> CELL_BKGD_LEVEL_NORMAL
                    }
                }

//				if (selectionAnimation != null) {
//					textView.startAnimation(selectionAnimation);
//				}

                if (gameWord.isAcross) {
                    col++
                } else {
                    row++
                }
            }
        }
    }

    /**
     * Fills puzzle textview with the character from the user's answer.
     *
     * @param gridCell the grid cell from which to get the textview and the user's answer.
     */
    private fun fillTextView(gridCell: GridCell) {
        fillTextView(gridCell.view, gridCell.dominantUserChar, gridCell.isDominantCharConfident)
    }

    /**
     * Fills puzzle textview with the character from the user's answer.
     *
     * @param textView the textview to fill.
     * @param dominantUserChar the character to fill the textview with.
     * @param confident true if confident color should be used, false otherwise.
     */
    private fun fillTextView(textView: TextView?, dominantUserChar: Char?, confident: Boolean) {
        textView?.apply {
            text = dominantUserChar?.toString()
            setTextColor(if (confident) CONFIDENT_COLOR else TENTATIVE_COLOR)
        }
    }

    /**
     * Creates textview for the puzzle layout.
     *
     * @param res resources.
     * @param fontHeight height of font in pixels.
     * @param size height of textview in pizels.
     * @return textview.
     */
    private fun createCellTextView(res: Resources, fontHeight: Float, size: Int): TextView {
        val textView = TextView(activity)
        textView.apply {
            gravity = Gravity.CENTER
            // need to create Drawable object for each TextView
            background = res.getDrawable(R.drawable.cell_drawable)  // using deprecated method since setBackground() not supported until API level 16
            setTextSize(TypedValue.COMPLEX_UNIT_PX, fontHeight)
            width = size
            height = size
            background.level = 1 // set normal text cell background (i.e. no error indication)
            //		setSoundEffectsEnabled(false); // true by default, consider disabling since we're providing our own vibration (except not all devices have vibration)
        }
        return textView
    }

    /**
     * Gets [GridCell] cell corresponding to the specified view.
     *
     * @param v view to get [GridCell] for.
     */
    private fun getCellForView(v: View): GridCell? {
        cellGrid.forEach {
            it.forEach {
                if (v === it?.view) {
                    return it
                }
            }
        }
        return null
    }

    //endregion

}