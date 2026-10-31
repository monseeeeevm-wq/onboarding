package com.banco.onboarding.integration.gestopago;

import com.banco.onboarding.exception.GestopagoRespuestaException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class GestopagoXmlParser {

    private static final String CODIGO_EXITO = "01";
    private static final String CODIGO_XML_INVALIDO = "XML_INVALIDO";

    public List<ProductoGestopago> parsearCatalogo(String xml) {
        Element raiz = leerRaiz(xml);
        validarMensaje(raiz);

        NodeList nodos = raiz.getElementsByTagName("producto");
        List<ProductoGestopago> productos = new ArrayList<>();
        int descartados = 0;
        for (int i = 0; i < nodos.getLength(); i++) {
            try {
                productos.add(aProducto((Element) nodos.item(i)));
            } catch (IllegalArgumentException e) {
                descartados++;
            }
        }
        if (descartados > 0) {
            log.warn("Se descartaron {} productos con datos inválidos del catálogo", descartados);
        }
        return List.copyOf(productos);
    }

    private ProductoGestopago aProducto(Element e) {
        return new ProductoGestopago(
                Integer.valueOf(e.getAttribute("idServicio")),
                e.getAttribute("servicio"),
                Integer.valueOf(e.getAttribute("idProducto")),
                e.getAttribute("producto"),
                Integer.valueOf(e.getAttribute("idCatTipoServicio")),
                new BigDecimal(e.getAttribute("precio")),
                e.getAttribute("tipoReferencia"),
                Boolean.parseBoolean(e.getAttribute("hasDigitoVerificador")),
                textoDe(e, "legend"));
    }

    private void validarMensaje(Element raiz) {
        NodeList mensajes = raiz.getElementsByTagName("MENSAJE");
        if (mensajes.getLength() == 0) {
            throw respuestaInvalida();
        }
        String codigo = textoDe((Element) mensajes.item(0), "CODIGO");
        if (!CODIGO_EXITO.equals(codigo)) {
            throw new GestopagoRespuestaException("El proveedor respondió con el código " + codigo, codigo);
        }
    }

    private Element leerRaiz(String xml) {
        if (xml == null || xml.isBlank()) {
            throw respuestaInvalida();
        }
        try {
            DocumentBuilderFactory fabrica = DocumentBuilderFactory.newInstance();
            fabrica.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            fabrica.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            fabrica.setXIncludeAware(false);
            fabrica.setExpandEntityReferences(false);
            Document documento = fabrica.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
            return documento.getDocumentElement();
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw respuestaInvalida();
        }
    }

    private String textoDe(Element padre, String etiqueta) {
        NodeList nodos = padre.getElementsByTagName(etiqueta);
        return nodos.getLength() == 0 ? "" : nodos.item(0).getTextContent().trim();
    }

    private GestopagoRespuestaException respuestaInvalida() {
        return new GestopagoRespuestaException("La respuesta del proveedor no es un XML válido", CODIGO_XML_INVALIDO);
    }
}