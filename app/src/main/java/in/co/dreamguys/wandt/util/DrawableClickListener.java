package in.co.dreamguys.wandt.util;

import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

/**
 * Created by Dreamguys on 12/18/2015.
 */
public abstract class DrawableClickListener implements View.OnTouchListener
{

    /* PUBLIC CONSTANTS */
    /**
     * This represents the left drawable.
     * */
    public static final int DRAWABLE_INDEX_LEFT = 0;
    /**
     * This represents the top drawable.
     * */
    public static final int DRAWABLE_INDEX_TOP = 1;
    /**
     * This represents the right drawable.
     * */
    public static final int DRAWABLE_INDEX_RIGHT = 2;
    /**
     * This represents the bottom drawable.
     * */
    public static final int DRAWABLE_INDEX_BOTTOM = 3;
    /**
     * This stores the default value to be used for the
     * {@link DrawableClickListener#fuzz}.
     * */
    public static final int DEFAULT_FUZZ = 10;

    /* PRIVATE VARIABLES */
    /**
     * This stores the number of pixels of &quot;fuzz&quot; that should be
     * included to account for the size of a finger.
     * */
    private final int fuzz;
    /**
     * This will store a reference to the {@link Drawable}.
     * */
    private Drawable drawable = null;


    public DrawableClickListener( final TextView view, final int drawableIndex )
    {
        this( view, drawableIndex, DrawableClickListener.DEFAULT_FUZZ );
    }
    public DrawableClickListener( final TextView view, final int drawableIndex, final int fuzz )
    {
        super();
        this.fuzz = fuzz;
        final Drawable[] drawables = view.getCompoundDrawables();
        if ( drawables != null && drawables.length == 4 )
        {
            this.drawable = drawables[drawableIndex];
        }
    }

    /* OVERRIDDEN PUBLIC METHODS */
    @Override
    public boolean onTouch( final View v, final MotionEvent event )
    {
        if ( event.getAction() == MotionEvent.ACTION_DOWN && drawable != null )
        {
            final int x = (int) event.getX();
            final int y = (int) event.getY();
            final Rect bounds = drawable.getBounds();
            if ( this.isClickOnDrawable( x, y, v, bounds, this.fuzz ) )
            {
                return this.onDrawableClick();
            }
        }
        return false;
    }

    /* PUBLIC METHODS */
    /**
     *
     * */
    public abstract boolean isClickOnDrawable( final int x, final int y, final View view, final Rect drawableBounds, final int fuzz );


    public abstract boolean onDrawableClick();


    public static abstract class LeftDrawableClickListener extends DrawableClickListener
    {
        public LeftDrawableClickListener( final TextView view )
        {
            super( view, DrawableClickListener.DRAWABLE_INDEX_LEFT );
        }


        public LeftDrawableClickListener( final TextView view, final int fuzz )
        {
            super( view, DrawableClickListener.DRAWABLE_INDEX_LEFT, fuzz );
        }

        /* PUBLIC METHODS */
        public boolean isClickOnDrawable( final int x, final int y, final View view, final Rect drawableBounds, final int fuzz )
        {
            if ( x >= ( view.getPaddingLeft() - fuzz ) )
            {
                if ( x <= ( view.getPaddingLeft() + drawableBounds.width() + fuzz ) )
                {
                    if ( y >= ( view.getPaddingTop() - fuzz ) )
                    {
                        if ( y <= ( view.getHeight() - view.getPaddingBottom() + fuzz ) )
                        {
                            return true;
                        }
                    }
                }
            }
            return false;
        }

    }
    public static abstract class TopDrawableClickListener extends DrawableClickListener
    {

        public TopDrawableClickListener( final TextView view )
        {
            super( view, DrawableClickListener.DRAWABLE_INDEX_TOP );
        }


        public TopDrawableClickListener( final TextView view, final int fuzz )
        {
            super( view, DrawableClickListener.DRAWABLE_INDEX_TOP, fuzz );
        }

        /* PUBLIC METHODS */
        public boolean isClickOnDrawable( final int x, final int y, final View view, final Rect drawableBounds, final int fuzz )
        {
            if ( x >= ( view.getPaddingLeft() - fuzz ) )
            {
                if ( x <= ( view.getWidth() - view.getPaddingRight() + fuzz ) )
                {
                    if ( y >= ( view.getPaddingTop() - fuzz ) )
                    {
                        if ( y <= ( view.getPaddingTop() + drawableBounds.height() + fuzz ) )
                        {
                            return true;
                        }
                    }
                }
            }
            return false;
        }

    }
    public static abstract class RightDrawableClickListener extends DrawableClickListener
    {


        public RightDrawableClickListener( final TextView view )
        {
            super( view, DrawableClickListener.DRAWABLE_INDEX_RIGHT );
        }


        public RightDrawableClickListener( final TextView view, final int fuzz )
        {
            super( view, DrawableClickListener.DRAWABLE_INDEX_RIGHT, fuzz );
        }

        /* PUBLIC METHODS */
        public boolean isClickOnDrawable( final int x, final int y, final View view, final Rect drawableBounds, final int fuzz )
        {
            if ( x >= ( view.getWidth() - view.getPaddingRight() - drawableBounds.width() - fuzz ) )
            {
                if ( x <= ( view.getWidth() - view.getPaddingRight() + fuzz ) )
                {
                    if ( y >= ( view.getPaddingTop() - fuzz ) )
                    {
                        if ( y <= ( view.getHeight() - view.getPaddingBottom() + fuzz ) )
                        {
                            return true;
                        }
                    }
                }
            }
            return false;
        }

    }
    public static abstract class BottomDrawableClickListener extends DrawableClickListener
    {
        public BottomDrawableClickListener( final TextView view )
        {
            super( view, DrawableClickListener.DRAWABLE_INDEX_BOTTOM );
        }
        public BottomDrawableClickListener( final TextView view, final int fuzz )
        {
            super( view, DrawableClickListener.DRAWABLE_INDEX_BOTTOM, fuzz );
        }

        /* PUBLIC METHODS */
        public boolean isClickOnDrawable( final int x, final int y, final View view, final Rect drawableBounds, final int fuzz )
        {
            if ( x >= ( view.getPaddingLeft() - fuzz ) )
            {
                if ( x <= ( view.getWidth() - view.getPaddingRight() + fuzz ) )
                {
                    if ( y >= ( view.getHeight() - view.getPaddingBottom() - drawableBounds.height() - fuzz ) )
                    {
                        if ( y <= ( view.getHeight() - view.getPaddingBottom() + fuzz ) )
                        {
                            return true;
                        }
                    }
                }
            }
            return false;
        }

    }

}
