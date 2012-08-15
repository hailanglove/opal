/*******************************************************************************
 * Copyright (c) 2012 Laurent CARON. All rights reserved. 
 * This program and the accompanying materials are made available under the terms 
 * of the Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Laurent CARON (laurent.caron at gmail dot com) - initial API and implementation
 *******************************************************************************/
package org.mihalis.opal.roundedToolbar;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Widget;
import org.mihalis.opal.utils.AdvancedPath;
import org.mihalis.opal.utils.SWTGraphicUtil;

/**
 * Instances of this class represent a selectable user interface object that represents a button in a rounded tool bar.
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>(none)</dd>
 * <dt><b>Events:</b></dt>
 * <dd>Selection</dd>
 * </dl>
 * 
 * @see <a href="http://www.eclipse.org/swt/snippets/#toolbar">ToolBar, ToolItem snippets</a>
 */
public class RoundedToolItem extends Item {

	private static final int MARGIN = 4;
	private static Color START_GRADIENT_COLOR = SWTGraphicUtil.createDisposableColor(70, 70, 70);
	private static Color END_GRADIENT_COLOR = SWTGraphicUtil.createDisposableColor(116, 116, 116);

	private final RoundedToolbar parentToolbar;
	private final List<SelectionListener> selectionListeners;
	private Rectangle bounds;
	private boolean enabled;
	private boolean selection;
	private int width;
	private int height;
	private Image disabledImage;
	private Image selectionImage;
	private int alignment;
	private Color textColorSelected;
	private Color textColor;
	private String tooltipText;
	private GC gc;
	private int toolbarHeight;
	private boolean isLast;

	/**
	 * Constructs a new instance of this class given its parent (which must be a <code>ToolBar</code>) 
	 * and a style value describing its behavior and appearance. The item is added to the end of the 
	 * items maintained by its parent.
	 * <p>
	 * The style value is either one of the style constants defined in class <code>SWT</code> which is 
	 * applicable to instances of this class, or must be built by <em>bitwise OR</em>'ing together 
	 * (that is, using the <code>int</code> "|" operator) two or more of those <code>SWT</code> style constants. 
	 * The class description lists the style constants that are applicable to the class. Style bits are 
	 * also inherited from superclasses.
	 * </p>
	 * 
	 * @param parent a composite control which will be the parent of the new instance (cannot be null)
	 * 
	 * @exception IllegalArgumentException <ul>
	 *     <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
	 * </ul>
	 * @exception SWTException <ul>
	 *     <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
	 *     <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed subclass</li>
	 * </ul>
	 * 
	 * @see Widget#getStyle
	 */
	public RoundedToolItem(final RoundedToolbar parent) {
		this(parent, SWT.NONE);
	}

	/**
	 * Constructs a new instance of this class given its parent (which must be a <code>ToolBar</code>) 
	 * and a style value describing its behavior and appearance. The item is added to the end of the 
	 * items maintained by its parent.
	 * <p>
	 * The style value is either one of the style constants defined in class <code>SWT</code> which is 
	 * applicable to instances of this class, or must be built by <em>bitwise OR</em>'ing together 
	 * (that is, using the <code>int</code> "|" operator) two or more of those <code>SWT</code> 
	 * style constants. The class description lists the style constants that are applicable to the class. 
	 * Style bits are also inherited from superclasses.
	 * </p>
	 * 
	 * @param parent a composite control which will be the parent of the new instance (cannot be null)
	 * @param style the style of control to construct
	 * 
	 * @exception IllegalArgumentException
	 * <ul>
	 *     <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
	 * </ul>
	 * @exception SWTException
	 * <ul>
	 *     <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
	 *     <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed subclass</li>
	 * </ul>
	 * 
	 * @see Widget#getStyle
	 */
	public RoundedToolItem(final RoundedToolbar parent, final int style) {
		super(parent, style);
		parent.addItem(this);
		parentToolbar = parent;
		textColor = parent.getDisplay().getSystemColor(SWT.COLOR_BLACK);
		textColorSelected = parent.getDisplay().getSystemColor(SWT.COLOR_WHITE);
		enabled = true;
		alignment = SWT.CENTER;
		selectionListeners = new ArrayList<SelectionListener>();
		width = -1;
		height = -1;
	}

	/**
	 * Adds the listener to the collection of listeners who will be notified when the control 
	 * is selected by the user, by sending it one of the messages defined in the 
	 * <code>SelectionListener</code> interface.
	 * <p>
	 * <code>widgetDefaultSelected</code> is not called.
	 * </p>
	 * 
	 * @param listener the listener which should be notified when the control is selected by the user,
	 * 
	 * @exception IllegalArgumentException <ul>
	 *     <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
	 * </ul>
	 * @exception SWTException <ul>
	 *     <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *     <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 * </ul>
	 * 
	 * @see SelectionListener
	 * @see #removeSelectionListener
	 * @see SelectionEvent
	 */
	public void addSelectionListener(final SelectionListener listener) {
		checkWidget();
		if (listener == null) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}
		this.selectionListeners.add(listener);
	}

	/**
	 * @return the default size of the item
	 */
	Point computeDefaultSize() {
		final Point sizeOfTextAndImages = computeSizeOfTextAndImages();
		return new Point(2 * MARGIN + sizeOfTextAndImages.x, 2 * MARGIN + sizeOfTextAndImages.y);
	}

	private Point computeSizeOfTextAndImages() {
		int width = 0, height = 0;
		final boolean textNotEmpty = getText() != null && !getText().equals("");

		if (textNotEmpty) {
			final GC gc = new GC(parentToolbar);
			final Point extent = gc.stringExtent(getText());
			gc.dispose();
			width += extent.x;
			height = extent.y;
		}

		final Point imageSize = new Point(-1, -1);
		computeImageSize(getImage(), imageSize);
		computeImageSize(selectionImage, imageSize);
		computeImageSize(disabledImage, imageSize);

		if (imageSize.x != -1) {
			width += imageSize.x;
			height = Math.max(imageSize.y, height);
			if (textNotEmpty) {
				width += MARGIN;
			}
		}
		return new Point(width, height);
	}

	private void computeImageSize(final Image image, final Point imageSize) {
		if (image == null) {
			return;
		}
		final Rectangle imageBounds = image.getBounds();
		imageSize.x = Math.max(imageBounds.width, imageSize.x);
		imageSize.y = Math.max(imageBounds.height, imageSize.y);
	}

	/**
	 * @see org.eclipse.swt.widgets.Widget#dispose()
	 */
	@Override
	public void dispose() {
		super.dispose();
		getParent().removeItem(this);
	}

	void drawButton(final GC gc, final int x, final int toolbarHeight, final boolean isLast) {
		this.gc = gc;
		this.toolbarHeight = toolbarHeight;
		this.isLast = isLast;

		if (selection) {
			drawBackground(x);
		}
		if (!isLast) {
			drawRightLine(x);
		}

		int xPosition = computeStartingPosition(x);

		xPosition += drawImage(x + xPosition);
		drawText(x + xPosition);

		bounds = new Rectangle(x, 0, width, toolbarHeight);
	}

	private void drawBackground(final int x) {
		final AdvancedPath path = new AdvancedPath(getDisplay());
		if (isLast) {
			path.addRoundRectangle(0, 0, getParent().getBounds().width, toolbarHeight, parentToolbar.getCornerRadius(), parentToolbar.getCornerRadius());
		} else {
			path.addRoundRectangleStraightRight(x, 0, width, toolbarHeight, parentToolbar.getCornerRadius(), parentToolbar.getCornerRadius());
		}

		gc.setClipping(path);

		gc.setForeground(START_GRADIENT_COLOR);
		gc.setBackground(END_GRADIENT_COLOR);
		gc.fillGradientRectangle(x, 0, width + parentToolbar.getCornerRadius(), toolbarHeight, true);

		gc.setForeground(RoundedToolbar.BORDER_COLOR);
		gc.drawRoundRectangle(0, 0, width - 1, height - 1, parentToolbar.getCornerRadius(), parentToolbar.getCornerRadius());

		gc.setClipping((Rectangle) null);
	}

	private void drawRightLine(final int x) {
		gc.setForeground(RoundedToolbar.BORDER_COLOR);
		gc.drawLine(x + width, 0, x + width, toolbarHeight);
	}

	private int computeStartingPosition(final int x) {
		final int widthOfTextAndImage = computeSizeOfTextAndImages().x;
		switch (alignment) {
			case SWT.CENTER:
				return (width - widthOfTextAndImage) / 2;
			case SWT.RIGHT:
				return width - widthOfTextAndImage - MARGIN;
			default:
				return MARGIN;
		}
	}

	void fireSelectionEvent() {
		final Event event = new Event();
		event.widget = parentToolbar;
		event.display = getDisplay();
		event.item = this;
		event.type = SWT.Selection;
		for (final SelectionListener selectionListener : selectionListeners) {
			selectionListener.widgetSelected(new SelectionEvent(event));
		}
	}

	private int drawImage(final int xPosition) {
		Image image;
		if (!isEnabled()) {
			image = disabledImage;
		} else if (selection) {
			image = selectionImage;
		} else {
			image = getImage();
		}

		if (image == null) {
			return 0;
		}

		final int yPosition = (toolbarHeight - image.getBounds().height) / 2;
		gc.drawImage(image, xPosition, yPosition);
		return image.getBounds().width + MARGIN;
	}

	private void drawText(final int xPosition) {
		gc.setFont(parentToolbar.getFont());
		if (selection) {
			gc.setForeground(textColorSelected);
		} else {
			gc.setForeground(textColor);
		}

		final Point textSize = gc.stringExtent(getText());
		final int yPosition = (toolbarHeight - textSize.y) / 2;

		gc.drawText(getText(), xPosition, yPosition, true);
	}

	/**
	 * Returns a value which describes the position of the text in the receiver. 
	 * The value will be one of <code>LEFT</code>, <code>RIGHT</code> or <code>CENTER</code>.
	 * 
	 * @return the alignment
	 * 
	 * @exception SWTException <ul>
	 *     <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *     <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 * </ul>
	 */
	public int getAlignment() {
		checkWidget();
		return alignment;
	}

	/**
	 * Returns a rectangle describing the receiver's size and location relative to its parent 
	 * (or its display if its parent is null), unless the receiver is a shell. 
	 * In this case, the location is relative to the display.
	 * 
	 * @return the receiver's bounding rectangle
	 * 
	 * @exception SWTException <ul>
	 *     <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *     <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 * </ul>
	 */
	public Rectangle getBounds() {
		checkWidget();
		return this.bounds;
	}

	/**
	 * @return the image displayed when the button is disabled
	 * 
	 * @exception SWTException <ul>
	 *     <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *     <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 * </ul>
	 */
	public Image getDisabledImage() {
		checkWidget();
		return this.disabledImage;
	}

	/**
	 * Returns <code>true</code> if the receiver is enabled, and <code>false</code> otherwise. A disabled control is typically not selectable from the user interface and draws with an inactive or "grayed" look.
	 * 
	 * @return the receiver's enabled state
	 * 
	 * @exception SWTException <ul>
	 *     <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *     <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 * </ul>
	 * 
	 * @see #isEnabled
	 */
	public boolean getEnabled() {
		checkWidget();
		return this.enabled;
	}

	/**
	 * Returns the whole height of the widget.
	 * 
	 * @return the receiver's height
	 * 
	 * @exception SWTException <ul>
	 *     <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *     <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 * </ul>
	 */
	public int getHeight() {
		checkWidget();
		if (this.height == -1) {
			return this.computeDefaultSize().y;
		}
		return this.height;
	}

	/**
	 * Returns the receiver's parent, which must be a <code>RoundedToolBar</code>.
	 * 
	 * @return the receiver's parent
	 * 
	 * @exception SWTException <ul>
	 *     <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *     <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 * </ul>
	 */
	public RoundedToolbar getParent() {
		checkWidget();
		return this.parentToolbar;
	}

	/**
	 * Returns <code>true</code> if the receiver is selected, and false otherwise.
	 * 
	 * @return the selection state
	 * 
	 * @exception SWTException <ul>
	 *     <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *     <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 * </ul>
	 */
	public boolean getSelection() {
		checkWidget();
		return this.selection;
	}

	/**
	 * @return the image displayed when the button is selected
	 * 
	 * @exception SWTException <ul>
	 *     <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *     <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 * </ul>
	 */
	public Image getSelectionImage() {
		checkWidget();
		return this.selectionImage;
	}

	/**
	 * Returns the color of the text when the button is enabled and not selected.
	 * 
	 * @return the receiver's text color
	 * 
	 * @exception SWTException <ul>
	 *     <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *     <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 * </ul>
	 */
	public Color getTextColor() {
		checkWidget();
		return textColor;
	}

	/**
	 * Returns the color of the text when the button is not selected.
	 * 
	 * @return the receiver's text color
	 * 
	 * @exception SWTException <ul>
	 *     <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *     <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 * </ul>
	 */

	public Color getTextColorSelected() {
		checkWidget();
		return textColorSelected;
	}

	/**
	 * Returns the receiver's tool tip text, or null if it has not been set.
	 * 
	 * @return the receiver's tool tip text
	 * 
	 * @exception SWTException <ul>
	 *     <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *     <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 * </ul>
	 */
	public String getTooltipText() {
		checkWidget();
		return tooltipText;
	}

	/**
	 * Returns the whole height of the widget.
	 * 
	 * @return the receiver's height
	 * 
	 * @exception SWTException <ul>
	 *     <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *     <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 * </ul>
	 */
	public int getWidth() {
		checkWidget();
		if (this.width == -1) {
			return this.computeDefaultSize().x;
		}
		return this.width;
	}

	/**
	 * Returns <code>true</code> if the receiver is enabled, and <code>false</code> otherwise. 
	 * A disabled control is typically not selectable from the user interface and draws 
	 * with an inactive or "grayed" look.
	 * 
	 * @return the receiver's enabled state
	 * 
	 * @exception SWTException <ul>
	 *     <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *     <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 * </ul>
	 * 
	 * @see #getEnabled
	 */
	public boolean isEnabled() {
		checkWidget();
		return this.enabled;
	}

	/**
	 * Removes the listener from the collection of listeners who will be notified when the 
	 * control is selected by the user.
	 * 
	 * @param listener the listener which should no longer be notified
	 * 
	 * @exception IllegalArgumentException <ul>
	 *     <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
	 * </ul>
	 * @exception SWTException <ul>
	 *     <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *     <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 * </ul>
	 * 
	 * @see SelectionListener
	 * @see #addSelectionListener
	 */
	public void removeSelectionListener(final SelectionListener listener) {
		checkWidget();
		if (listener == null) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}
		this.selectionListeners.remove(listener);
	}

	/**
	 * Controls how text will be displayed in the receiver. The argument should 
	 * be one of <code>LEFT</code>, <code>RIGHT</code> or <code>CENTER</code>.
	 * 
	 * @param alignment the new alignment
	 * 
	 * @exception SWTException <ul>
	 *     <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *     <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 * </ul>
	 */
	public void setAlignment(final int alignment) {
		checkWidget();
		this.alignment = alignment;
	}

	/**
	 * Sets the receiver's size and location to the rectangular area specified by the argument. 
	 * The <code>x</code> and <code>y</code> fields of the rectangle are relative to the receiver's 
	 * parent (or its display if its parent is null).
	 * <p>
	 * Note: Attempting to set the width or height of the receiver to a negative number will cause that value to be set to zero 
	 * instead.
	 * </p>
	 * 
	 * @param rect the new bounds for the receiver
	 * 
	 * @exception SWTException <ul>
	 *     <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *     <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 * </ul>
	 */
	public void setBounds(final Rectangle rectangle) {
		checkWidget();
		if (bounds == null) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}

		this.bounds = new Rectangle(Math.max(0, rectangle.x), //
				Math.max(0, rectangle.y), //
				Math.max(0, rectangle.width), //
				Math.max(0, rectangle.height));

	}

	/**
	 * Sets the receiver's image to the argument when this is one is disabled, which may be null indicating that no image should be displayed.
	 * 
	 * @param image the image to display on the receiver (may be null)
	 * 
	 * @exception IllegalArgumentException <ul>
	 *     <li>ERROR_INVALID_ARGUMENT - if the image has been disposed</li>
	 * </ul>
	 * @exception SWTException <ul>
	 *     <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *     <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 * </ul>
	 */
	public void setDisabledImage(final Image image) {
		checkWidget();
		this.disabledImage = image;
	}

	/**
	 * Enables the receiver if the argument is <code>true</code>, and disables it otherwise.
	 * <p>
	 * A disabled control is typically not selectable from the user interface and draws with an inactive or "grayed" look.
	 * </p>
	 * 
	 * @param enabled the new enabled state
	 * 
	 * @exception SWTException <ul>
	 *     <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *     <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 * </ul>
	 */
	public void setEnabled(final boolean enabled) {
		checkWidget();
		this.enabled = enabled;
	}

	/**
	 * Sets the height of the receiver.
	 * <p>
	 * Note: Attempting to set the width or height of the receiver to a negative number will cause that value to be set to zero 
	 * instead.
	 * </p>
	 * @param height the new width
	 * 
	 * @exception SWTException <ul>
	 *     <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *     <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 * </ul>
	 */
	public void setHeight(final int height) {
		checkWidget();
		this.height = Math.max(height, 0);
	}

	/**
	 * Sets the selection state of the receiver.
	 * 
	 * @param selected the new selection state
	 * 
	 * @exception SWTException <ul>
	 *     <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *     <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 * </ul>
	 */
	public void setSelection(final boolean selected) {
		checkWidget();
		this.selection = selected;
	}

	/**
	 * Sets the receiver's image to the argument when this one is selected, which may be 
	 * null indicating that no image should be displayed.
	 * 
	 * @param image the image to display on the receiver (may be null)
	 * 
	 * @exception IllegalArgumentException <ul>
	 *     <li>ERROR_INVALID_ARGUMENT - if the image has been disposed</li>
	 * </ul>
	 * @exception SWTException <ul>
	 *     <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *     <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 * </ul>
	 */
	public void setSelectionImage(final Image image) {
		checkWidget();
		this.selectionImage = image;
	}

	/**
	 * Sets the receiver's text color to the argument, which may be null indicating that no image should be displayed.
	 * 
	 * @param textColor the text color to display on the receiver (may be null)
	 * 
	 * @exception IllegalArgumentException <ul>
	 *     <li>ERROR_INVALID_ARGUMENT - if the image has been disposed</li>
	 * </ul>
	 * @exception SWTException <ul>
	 *     <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *     <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 * </ul>
	 */
	public void setTextColor(final Color textColor) {
		checkWidget();
		this.textColor = textColor;
	}

	/**
	 * Sets the receiver's text color to the argument when this one is selected, which may be null indicating that no image should be displayed.
	 * 
	 * @param textColor the text color to display on the receiver (may be null)
	 * 
	 * @exception IllegalArgumentException <ul>
	 *     <li>ERROR_INVALID_ARGUMENT - if the image has been disposed</li>
	 * </ul>
	 * @exception SWTException <ul>
	 *     <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *     <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 * </ul>
	 */

	public void setTextColorSelected(final Color textColor) {
		checkWidget();
		this.textColorSelected = textColor;
	}

	/**
	 * Sets the receiver's tool tip text to the argument, which may be null indicating 
	 * that the default tool tip for the control will be shown. For a control that has 
	 * a default tool tip, such as the Tree control on Windows, setting the tool tip text to an
	 * empty string replaces the default, causing no tool tip text to be shown.
	 * <p>
	 * The mnemonic indicator (character '&amp;') is not displayed in a tool tip. To display 
	 * a single '&amp;' in the tool tip, the character '&amp;' can be escaped by doubling it in the string.
	 * </p>
	 * 
	 * @param string the new tool tip text (or null)
	 * 
	 * @exception SWTException <ul>
	 *     <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *     <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 * </ul>
	 */
	public void setTooltipText(final String string) {
		checkWidget();
		this.tooltipText = (string == null ? "" : string);
	}

	/**
	 * Sets the width of the receiver.
	 * <p>
	 * Note: Attempting to set the width or height of the receiver to a negative number will cause that value to be set to zero 
	 * instead.
	 * </p>
	 * @param width the new width
	 * 
	 * @exception SWTException <ul>
	 *     <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *     <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 * </ul>
	 */
	public void setWidth(final int width) {
		checkWidget();
		this.width = Math.max(0, width);
	}
}