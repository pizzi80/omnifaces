package org.omnifaces.renderkit;

import java.util.Iterator;

import jakarta.faces.context.FacesContext;
import jakarta.faces.render.RenderKit;
import jakarta.faces.render.RenderKitFactory;

/**
 * This render kit factory takes care that the {@link OmniRenderKit} is properly initialized.
 *
 * @author Bauke Scholtz
 * @since 5.0
 * @see OmniRenderKit
 */
public class OmniRenderKitFactory extends RenderKitFactory {

    public OmniRenderKitFactory(RenderKitFactory wrapped) {
        super(wrapped);
    }

    @Override
    public void addRenderKit(String renderKitId, RenderKit renderKit) {
        getWrapped().addRenderKit(renderKitId, renderKit);
    }

    @Override
    public RenderKit getRenderKit(FacesContext context, String renderKitId) {
        var renderKit = getWrapped().getRenderKit(context, renderKitId);
        return HTML_BASIC_RENDER_KIT.equals(renderKitId) ? new OmniRenderKit(renderKit) : renderKit;
    }

    @Override
    public Iterator<String> getRenderKitIds() {
        return getWrapped().getRenderKitIds();
    }
}