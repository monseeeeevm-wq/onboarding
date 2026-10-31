package com.banco.onboarding.integration.gestopago;

import com.banco.onboarding.exception.GestopagoRespuestaException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GestopagoXmlParserTest {

    private final GestopagoXmlParser parser = new GestopagoXmlParser();

    private static final String XML_VALIDO = """
            <?xml version='1.0' encoding='UTF-8'?>
            <RESPONSE>
                <MENSAJE><CODIGO>01</CODIGO><TEXTO>Operacion realizada con exito</TEXTO></MENSAJE>
                <PRODUCTOS>
                    <producto servicio='AGUAKAN (Cancun)' producto='Agua Cancun' idServicio='56' idProducto='185' idCatTipoServicio='15' tipoFront='2' hasDigitoVerificador='false' precio='10.0' showAyuda='false' tipoReferencia='c'>
                        <legend><![CDATA[Atencion a clientes 073]]></legend>
                    </producto>
                </PRODUCTOS>
            </RESPONSE>
            """;

    @Test
    void parsearCatalogo_xmlValido_devuelveLosProductos() {
        List<ProductoGestopago> productos = parser.parsearCatalogo(XML_VALIDO);

        assertThat(productos).hasSize(1);
        ProductoGestopago p = productos.get(0);
        assertThat(p.idProducto()).isEqualTo(185);
        assertThat(p.servicio()).isEqualTo("AGUAKAN (Cancun)");
        assertThat(p.precio()).isEqualByComparingTo("10.0");
        assertThat(p.leyenda()).isEqualTo("Atencion a clientes 073");
    }

    @Test
    void parsearCatalogo_codigoDistintoDeExito_lanzaExcepcionConElCodigo() {
        String xml = "<RESPONSE><MENSAJE><CODIGO>05</CODIGO><TEXTO>Error</TEXTO></MENSAJE></RESPONSE>";

        GestopagoRespuestaException error =
                assertThrows(GestopagoRespuestaException.class, () -> parser.parsearCatalogo(xml));

        assertThat(error.getCodigoProveedor()).isEqualTo("05");
    }

    @Test
    void parsearCatalogo_xmlMalFormado_lanzaExcepcion() {
        assertThrows(GestopagoRespuestaException.class, () -> parser.parsearCatalogo("<RESPONSE><MENSAJE>"));
    }

    @Test
    void parsearCatalogo_respuestaVacia_lanzaExcepcion() {
        assertThrows(GestopagoRespuestaException.class, () -> parser.parsearCatalogo("  "));
    }

    @Test
    void parsearCatalogo_xmlConDoctype_seRechazaPorSeguridad() {
        String xml = "<?xml version='1.0'?><!DOCTYPE x [<!ENTITY e SYSTEM 'file:///etc/passwd'>]>"
                + "<RESPONSE><MENSAJE><CODIGO>01</CODIGO></MENSAJE></RESPONSE>";

        assertThrows(GestopagoRespuestaException.class, () -> parser.parsearCatalogo(xml));
    }

    @Test
    void parsearCatalogo_productoConPrecioInvalido_seDescartaYSeConservanLosDemas() {
        String xml = """
                <RESPONSE>
                    <MENSAJE><CODIGO>01</CODIGO></MENSAJE>
                    <PRODUCTOS>
                        <producto servicio='A' producto='Malo' idServicio='1' idProducto='1' idCatTipoServicio='1' hasDigitoVerificador='false' precio='abc' tipoReferencia='c'/>
                        <producto servicio='B' producto='Bueno' idServicio='2' idProducto='2' idCatTipoServicio='1' hasDigitoVerificador='false' precio='50.0' tipoReferencia='c'/>
                    </PRODUCTOS>
                </RESPONSE>
                """;

        List<ProductoGestopago> productos = parser.parsearCatalogo(xml);

        assertThat(productos).hasSize(1);
        assertThat(productos.get(0).producto()).isEqualTo("Bueno");
    }
}