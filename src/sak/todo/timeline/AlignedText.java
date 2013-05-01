package sak.todo.timeline;

import java.util.StringTokenizer;

import org.anddev.andengine.entity.text.ChangeableText;
import org.anddev.andengine.opengl.font.Font;
import org.anddev.andengine.util.HorizontalAlign;
import org.anddev.andengine.util.VerticalAlign;

public class AlignedText extends ChangeableText {

	private HorizontalAlign mAlignmentH;
	private VerticalAlign mAlignmentV;

	private float offsetX = 0.0f;
	private float offsetY = 0.0f;

	private int screenWidth;
	private int screenHeight;
	
	private Font font;
	
	private static final int TEXT_PADING = 8;

	public AlignedText(float pOffsetX, float pOffsetY, Font pFont,
			String pText, HorizontalAlign pHorizontalAlign,
			VerticalAlign pVerticalAlign, int pScreenWidth, int pScreenHeight) {
		
		
		super(pOffsetX, pOffsetY, pFont, pText, pHorizontalAlign, 255);
		
		this.font = pFont;
		
		this.mAlignmentH = pHorizontalAlign;
		this.mAlignmentV = pVerticalAlign;

		this.offsetX = pOffsetX;
		this.offsetY = pOffsetY;

		this.screenWidth = pScreenWidth;
		this.screenHeight = pScreenHeight;

		this.alignText();
//		this.fitToWidth();
	}

	public void alignText() {
		float textwidth = getWidth();
		float textheight = getHeight();

		float x = offsetX;
		float y = offsetY;

		if (mAlignmentH == HorizontalAlign.CENTER) {
			x += ((this.screenWidth / 2) - (textwidth / 2));
		}

		if (mAlignmentH == HorizontalAlign.RIGHT) {
			x += (this.screenWidth - textwidth);
		}

		if (mAlignmentV == VerticalAlign.CENTER) {
			y += ((this.screenHeight / 2) - (textheight / 2));
		}

		if (mAlignmentV == VerticalAlign.BOTTOM) {
			y += (this.screenHeight - textheight);
		}

		setPosition(x, y);
	}
	
	private void fitToWidth(){
		StringTokenizer st = new StringTokenizer(getText(), " ");
		StringBuilder sb = new StringBuilder(getText().length() + st.countTokens());
		
		int maxSize = screenWidth / (int)font.getLetter('x').mTextureWidth;
		int lineSize = 0;
		while (st.hasMoreTokens()) {
			String word = st.nextToken();
			
			if(lineSize == 0){
				sb.append(word + ' ');
				lineSize = word.length() + 1;
			}else if(lineSize + word.length() > maxSize){
				sb.append('\n');
				sb.append(word+' ');
				lineSize = word.length() + 1;
			}else{
				sb.append(word+' ');
				lineSize += word.length() + 1;
			}
		}
		
		setText(sb.toString());
	}
	@Override
	public void setText(String pText) {
		super.setText(pText);
		alignText();
	}
}