// Clase en la que se dibujará
package svg_viewer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.Document;
import javax.swing.JComponent;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SVGDiagram extends JComponent {

    private final Document doc; // Documento DOM con los componentes del dibujo
    private final Element root; // Elemento raiz (SVG)
    private final int svgW;     // ancho del dibujo
    private final int svgH;     // alto del dibujo

    private Properties webColors; // Permite almacenar y recuperar propiedades en dado caso que quiera cambiar parámetros o variables en mi app

    public SVGDiagram(Document svgDoc) {
        super();

        doc = svgDoc;

        root = doc.getDocumentElement();  // Raíz del documento

        // establecer dimensiones del dibujo
        svgW = Integer.parseInt(root.getAttribute("width"));
        svgH = Integer.parseInt(root.getAttribute("height"));

        // establecer colorCode de fondo
        this.setBackground(Color.white);

        loadColors();
    }

    // Método para cargar el archivo colors.properties al objeto Properties
    private void loadColors() {
        try {
            String userDir = System.getProperty("user.dir");                      // Directorio por default
            FileReader reader = new FileReader(userDir + "/colors.properties");   // Ubicación del archivo

            webColors = new Properties();     // Crear el objeto properties
            webColors.load(reader);           // Se carga a partir del archivo colors

        } catch (FileNotFoundException ex) {
            Logger.getLogger(SVGApplication.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SVGApplication.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // Pinta el dibujo cada vez que se requiera
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;

        // Pintar el fondo del área del dibujo
        g2.setColor(getBackground());

        // Pintar un rectángulo en toda el área
        g2.fillRect(0, 0, getWidth(), getHeight());
        
        // Obtener cada una de las figuras del dibujo
        NodeList list = root.getChildNodes();
        
        int n = list.getLength();
        Element element;
        for (int i = 0; i < n; i++) {
            Node nodo = list.item(i);

            if (nodo.getNodeType() == Node.ELEMENT_NODE) {
                element = (Element) nodo;

                // Qué tipo de figura es?
                String name = element.getTagName();

                if (name.equals("line")) {
                    drawLine(element, g);     // dibujar una linea
                }
                if (name.equals("rect")) {
                    drawRect(element, g);     // dibujar un rectángulo/cuadrado
                }
                if (name.equals("text")) {
                    drawText(element, g);     // dibujar texto
                }
                if (name.equals("circle") || name.equals("ellipse")) {
                    drawEllipse(element, g);  // dibujar circulo
                }
                if (name.equals("polyline")) {
                    drawPolyLine(element, g);  // dibujar polyline
                }
            }
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(svgW, svgH);
    }

    private void drawLine(Element line, Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        // Grosor de línea
        if (line.hasAttribute("stroke-width")) {
            float sw = Float.parseFloat(line.getAttribute("stroke-width")); // Especificar grosor de linea
            g2d.setStroke(new BasicStroke(sw));                             // Asignarle el grosor indicado
        } else {
            g2d.setStroke(new BasicStroke(1));                              // Grosor por default: 1 pixel
        }

        // Color particular (RGB, red-green-blue)
        if (line.hasAttribute("stroke")) {
            String colorCode = line.getAttribute("stroke"); // Especificar colorCode de linea
            g2d.setColor(webColor(colorCode));
        } else {
            g2d.setColor(Color.BLACK);                      // colorCode por default
        }

        // coordenadas de la linea
        int x1 = (int) Double.parseDouble(line.getAttribute("x1"));
        int y1 = (int) Double.parseDouble(line.getAttribute("y1"));

        int x2 = (int) Double.parseDouble(line.getAttribute("x2"));
        int y2 = (int) Double.parseDouble(line.getAttribute("y2"));

        // Dibujar línea
        g2d.draw(new Line2D.Double(x1, y1, x2, y2));
    }

    private void drawRect(Element rect, Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        
        // Coordenada de posición (verificar que se haya dado)
        double rx, ry, x, y, width, height;
        rx = ry = x = y = width = height = 0.0;

        if (!rect.getAttribute("x").isEmpty()) {
            x = Double.parseDouble(rect.getAttribute("x"));
        }

        if (!rect.getAttribute("y").isEmpty()) {
            y = Double.parseDouble(rect.getAttribute("y"));
        }

        if (!rect.getAttribute("width").isEmpty()) {
            width = Double.parseDouble(rect.getAttribute("width"));
        }

        if (!rect.getAttribute("height").isEmpty()) {
            height = Double.parseDouble(rect.getAttribute("height"));
        }

        if (rect.hasAttribute("rx")) {
            rx = Double.parseDouble(rect.getAttribute("rx"));
        }

        if (rect.hasAttribute("ry")) {
            ry = Double.parseDouble(rect.getAttribute("ry"));
        }
        
        // RELLENO
        
        if (rect.hasAttribute("fill")) {
            String colorFill = rect.getAttribute("fill");                   // Especificar color de relleno
            
            // Si no es "none" rellenar del color definido
            if (!colorFill.equals("none")) {
                g2d.setColor(webColor(colorFill));
 
                if (rect.hasAttribute("rx") && rect.hasAttribute("ry")) {   // Verificar si tiene esquinas redondeadas
                    g2d.fill(new RoundRectangle2D.Double(x, y, width, height, rx, ry));
                } else {
                    g2d.fill(new Rectangle2D.Double(x, y, width, height));
                }
                
            } 
        } else {                                                            // Si no tiene la etiqueta de relleno
            g2d.setColor(Color.BLACK);                                      // Relleno por default
            
            if (rect.hasAttribute("rx") && rect.hasAttribute("ry")) {       // Verificar si tiene esquinas redondeadas
                g2d.fill(new RoundRectangle2D.Double(x, y, width, height, rx, ry));
            } else {
                g2d.fill(new Rectangle2D.Double(x, y, width, height));
            }
        }
        
        // CONTORNO
        
        // Grosor del trazo
        if (rect.hasAttribute("stroke-width")) {
            float sw = Float.parseFloat(rect.getAttribute("stroke-width")); // Especificar grosor de linea
            g2d.setStroke(new BasicStroke(sw));                             // Asignarle el grosor indicado
        } 
        
        // Color particular (RGB, red-green-blue)
        if (rect.hasAttribute("stroke")) {
            String colorCode = rect.getAttribute("stroke");                 // Especificar colorCode de linea
            g2d.setColor(webColor(colorCode));
        } 
        
        if (rect.hasAttribute("rx") && rect.hasAttribute("ry")) {           // Verificar si tiene esquinas redondeadas
            g2d.draw(new RoundRectangle2D.Double(x, y, width, height, rx, ry));
        } else {
            g2d.draw(new Rectangle2D.Double(x, y, width, height));
        }
    }

    private void drawEllipse(Element ellipse, Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        // Coordenada de posición (verificar que se haya dado una posición)
        double r, rx, ry, cx, cy;
        r = rx = ry = cx = cy = 0.0;

        if (!ellipse.getAttribute("cx").isEmpty()) {
            cx = Double.parseDouble(ellipse.getAttribute("cx"));
        }

        if (!ellipse.getAttribute("cy").isEmpty()) {
            cy = Double.parseDouble(ellipse.getAttribute("cy"));
        }
        
        if (ellipse.hasAttribute("r")) {
            r = Double.parseDouble(ellipse.getAttribute("r"));
        }

        if (ellipse.hasAttribute("rx")) {
            rx = Double.parseDouble(ellipse.getAttribute("rx"));
        }

        if (ellipse.hasAttribute("ry")) {
            ry = Double.parseDouble(ellipse.getAttribute("ry"));
        }

        // RELLENO
        
        if (ellipse.hasAttribute("fill")) {
            String colorFill = ellipse.getAttribute("fill");                      // Especificar color de relleno
            
            // Si no es "none" rellenar del color definido
            if (!colorFill.equals("none")) {
                g2d.setColor(webColor(colorFill));
 
                g2d.fill(new Ellipse2D.Double(cx - rx, cy - ry, rx * 2, ry * 2)); // Dibujar elipse
                g2d.fill(new Ellipse2D.Double(cx - r, cy - r, r * 2, r * 2));     // Dibujar círculo                
            } 
            
        } else {                                                                  // Si no tiene la etiqueta de relleno
            g2d.setColor(Color.BLACK);                                            // Relleno por default
          
            if (ellipse.hasAttribute("rx")) {
                g2d.fill(new Ellipse2D.Double(cx - rx, cy - ry, rx * 2, ry * 2)); // Dibujar elipse
            } else {
                g2d.fill(new Ellipse2D.Double(cx - r, cy - r, r * 2, r * 2));     // Dibujar círculo
            }
        }

        
        // CONTORNO
        
        // Grosor de línea
        if (ellipse.hasAttribute("stroke-width")) {
            float sw = Float.parseFloat(ellipse.getAttribute("stroke-width"));// Especificar grosor
            g2d.setStroke(new BasicStroke(sw));                               // Asignarle el grosor indicado
        } 

        // Color particular (RGB, red-green-blue)
        if (ellipse.hasAttribute("stroke")) {
            String colorCode = ellipse.getAttribute("stroke");                // Especificar colorCode
            g2d.setColor(webColor(colorCode));
        }
            
        g2d.draw(new Ellipse2D.Double(cx - rx, cy - ry, rx * 2, ry * 2));     // Dibujar elipse
        g2d.draw(new Ellipse2D.Double(cx - r, cy - r, r * 2, r * 2));         // Dibujar círculo
    }

    private void drawText(Element text, Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        
        // coordenadas del string
        int x = (int) Double.parseDouble(text.getAttribute("x"));
        int y = (int) Double.parseDouble(text.getAttribute("y"));

        // Texto
        String texto = text.getFirstChild().getNodeValue();

        // Relleno
        if (text.hasAttribute("fill")) {
            String colorFill = text.getAttribute("fill");                  // Especificar color de relleno
            
            // Si no es "none" rellenar del color definido
            if (!colorFill.equals("none")) {
                g2d.setColor(webColor(colorFill));
            } 
            
        } else {                                                           // Si no tiene la etiqueta de relleno
            g2d.setColor(Color.BLACK);                                     // Relleno por default
        }

        int size = 0;
        if (text.hasAttribute("font-size")) {
            size = Integer.parseInt(text.getAttribute("font-size"));      
        }
        
        String fontFamily = null;
        if (text.hasAttribute("font-family")) {
            String font = text.getAttribute("font-family");               
            fontFamily = fontFamily(font);
        }
        
        int weight = 0;
        if (text.hasAttribute("font-weight")) {
            String fontWeight = text.getAttribute("font-weight"); 
            weight = fontWeight(fontWeight);
        }
        
        int style = 0;
        if (text.hasAttribute("font-style")) {
            String fontStyle = text.getAttribute("font-style"); 
            style = fontStyle(fontStyle);
        }
        

        // Crear fuente
        Font font = new Font(fontFamily, weight | style, size);
        g2d.setFont(font);
        
        // Dibujar texto
        g2d.drawString(texto, x, y); 
    } 
    
    // Método para asignarle el font-family de xml a java (Text)
    private String fontFamily(String tipo) {
        String font = null;
        if (tipo.equalsIgnoreCase("serif")) {
            font = "SERIF";
        }
        if (tipo.equalsIgnoreCase("sans-serif")) {
            font = "SANS_SERIF";
        }
        if (tipo.equalsIgnoreCase("monospace")) {
            font = "MONOSPACED";
        }
        return font;
    }
    
    // Método para asignarle el font-weight de xml a java (Text)
    private int fontWeight(String tipo) {
        int font = 0;
        if (tipo.equals("normal")) {
            font = 0;
        }
        if (tipo.equals("bold")) {
            font = 1;
        }
        return font;
    }
    
    // Método para asignarle el font-style de xml a java (Text)
    private int fontStyle(String tipo) {
        int font = 0;
        if (tipo.equalsIgnoreCase("italic")) {
            font = 2;
        }
        return font;
    }

    private void drawPolyLine(Element polyLine, Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        
        // Grosor de línea
        if (polyLine.hasAttribute("stroke-width")) {
            float sw = Float.parseFloat(polyLine.getAttribute("stroke-width"));// Especificar grosor
            g2d.setStroke(new BasicStroke(sw));                               // Asignarle el grosor indicado
        } 

        // Color particular (RGB, red-green-blue)
        if (polyLine.hasAttribute("stroke")) {
            String colorCode = polyLine.getAttribute("stroke");                // Especificar colorCode
            g2d.setColor(webColor(colorCode));
        }
        
        // Tomar puntos (coordenadas)
        String points = null;
        if (polyLine.hasAttribute("points")) {           
            points = polyLine.getAttribute("points");
        }
        
        // Dividir coordenadas
        Scanner scan = new Scanner(points);
        scan.useDelimiter(" ");                          // Dividir por espacios
        
        ArrayList<String> listaPuntos = new ArrayList(); // ArrayList para evitar definir el tamaño de un array
        while (scan.hasNext()) {
            String punto = scan.next();
            listaPuntos.add(punto);          // Agregar cada coordenada al ArrayList
        }

        int n = listaPuntos.size();          // No. de coordenadas
        int x[] = new int[n];                // Array para posiciones en x
        int y[] = new int[n];                // Array para posiciones en y
        
        int index = 0;
        while(index < n) {                  
            String punto = listaPuntos.get(index); // Tomar cada coordenada del arrayList
        
            Scanner t = new Scanner(punto);
            t.useDelimiter(",");                   // Dividir posición x y posición y
            
            x[index] = t.nextInt();                // Almacenar en arrays
            y[index] = t.nextInt();
                         
            index++;
        }
        
        g2d.drawPolyline(x, y, n);
    }

    private Color webColor(String colorString) {
        String colorCode = colorString.toLowerCase(); // Convertir a minúsculas
        Color newColor = null;

        // Saber qué formato se está utilizando para el color
        if (colorCode.startsWith("#")) {
            // Codigo de color "#00ffff"
            colorCode = colorCode.substring(1);
        } else if (colorCode.startsWith("0x")) {
            colorCode = colorCode.substring(2);
        } else if (colorCode.startsWith("rgb")) {
            // Codigo de color "rgb(00,255,0)"
            if (colorCode.startsWith("(", 3)) {
                return Color.BLACK;
            } else if (colorCode.startsWith("a(", 3)) {
                return Color.BLACK;
            }
        } else {
            colorCode = webColors.getProperty(colorCode).substring(1).trim();
        }

        // Sacar el valor que corresponde a cada color para crear el color indicado
        try {
            int r;
            int g;
            int b;
            int len = colorCode.length();

            if (len == 6) {
                r = Integer.parseInt(colorCode.substring(0, 2), 16); // 16 indica exadecimal
                g = Integer.parseInt(colorCode.substring(2, 4), 16);
                b = Integer.parseInt(colorCode.substring(4, 6), 16);

                newColor = new Color(r, g, b);
            }

        } catch (NumberFormatException nfe) {
            Logger.getLogger(SVGDiagram.class.getName()).log(Level.SEVERE, null, nfe);
        }

        return newColor;
    }

}
