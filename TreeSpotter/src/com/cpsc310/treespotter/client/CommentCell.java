package com.cpsc310.treespotter.client;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safecss.shared.SafeStyles;
import com.google.gwt.safecss.shared.SafeStylesUtils;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.HTML;

@SuppressWarnings("unused")
public class CommentCell extends AbstractCell<TreeComment> {

    /**
     * The HTML templates used to render the cell.
     */
    interface Templates extends SafeHtmlTemplates {
      /**
       * The template for this Cell, which includes styles and a value.
       * 
       * @param styles the styles to include in the style attribute of the div
       * @param value the safe value. Since the value type is {@link SafeHtml},
       *          it will not be escaped before including it in the template.
       *          Alternatively, you could make the value type String, in which
       *          case the value would be escaped.
       * @return a {@link SafeHtml} instance
       */
      @SafeHtmlTemplates.Template(
    		  "<div class ='comment'>" +
    		  "<span style='font-size: 13px; color:skyblue;'> {0} </span>" +
    		  "<span style='font-size: 13px; margin-left: 100px; color:skyblue;'> {1} </span>" +
    		  "<p style='display:block; padding: 10px;'> {2} </p>" +
    		  "</div>")
      SafeHtml commentCell(String name, String date, String text);
    }

    /**
     * Create a singleton instance of the templates used to render the cell.
     */
    private static Templates templates = GWT.create(Templates.class);

	@Override
	public void render(Context context, TreeComment comment, SafeHtmlBuilder sb) {
	      if (comment == null) {
	        return;
	      }
	      
	     SafeHtml rendered = templates.commentCell(comment.getUser(), comment.getDate(), comment.getCommentText());
	      sb.append(rendered);
		
	}

}
