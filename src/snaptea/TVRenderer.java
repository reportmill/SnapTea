/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snaptea;
import org.teavm.jso.dom.html.HTMLCanvasElement;
import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.typedarrays.Float32Array;
import org.teavm.jso.webgl.*;
import snap.gfx.Color;
import snap.gfx.Painter;
import snap.gfx3d.*;
import snap.util.SnapUtils;
import java.util.HashMap;
import java.util.Map;

/**
 * This Renderer subclass supports WebGL rendering.
 */
public class TVRenderer extends Renderer {

    // The canvas
    private HTMLCanvasElement  _canvas;

    // The WebGLRenderingContext
    protected WebGLRenderingContext  _gl;

    // A map of shader programs
    private Map<String,WebGLProgram>  _programs = new HashMap<>();

    // A map of vertex shaders
    private Map<String,WebGLShader>  _vertShaders = new HashMap<>();

    // A map of fragment shaders
    private Map<String,WebGLShader>  _fragShaders = new HashMap<>();

    // Canvas size in points
    private int  _canvasW, _canvasH;

    /**
     * Constructor.
     */
    public TVRenderer(Camera3D aCamera)
    {
        super(aCamera);
    }

    /**
     * Initialize canvas and context (HTMLCanvasElement and WebGLRenderingContext).
     */
    private void initCanvas(TVPainter aPainter)
    {
        // If Canvas already created, just bail (shouldn't be possible)
        if (_canvas != null) return;

        // Create canvas and size
        _canvas = (HTMLCanvasElement) HTMLDocument.current().createElement("canvas");
        resizeCanvas(aPainter);

        // Get WebGL context (if missing, complain and return)
        _gl = (WebGLRenderingContext) _canvas.getContext("webgl");
        if (_gl == null) {
            System.err.println("TVRenderer.initRenderer: canvas getContext() returned null");
            return;
        }

        // Initialize OpenGL
        _gl.clearColor(0f, 0f, 0f, 0f);
        _gl.enable(_gl.DEPTH_TEST);
        _gl.enable(_gl.CULL_FACE);
    }

    /**
     * Resize Canvas.
     */
    private void resizeCanvas(TVPainter aPainter)
    {
        // If Camera.ViewSize matches canvas size, just return - Should probably be checking scale, too
        Camera3D camera = getCamera();
        int viewW = (int) Math.round(camera.getViewWidth());
        int viewH = (int) Math.round(camera.getViewHeight());
        if (viewW == _canvasW && viewH == _canvasH)
            return;

        // Get Canvas size in points and pixels
        _canvasW = viewW;
        _canvasH = viewH;
        int scale = aPainter._scale;
        int canvasPixW = _canvasW * scale;
        int canvasPixH = _canvasH * scale;

        // Set Canvas size in points and pixels
        _canvas.setWidth(canvasPixW);
        _canvas.setHeight(canvasPixH);
        _canvas.getStyle().setProperty("width", _canvasW + "px");
        _canvas.getStyle().setProperty("height", _canvasH + "px");

        // If Context already around, resize viewport
        if (_gl != null)
            _gl.viewport(0, 0, canvasPixW, canvasPixH);
    }

    /**
     * Override to return name.
     */
    @Override
    public String getName()  { return "WebGL"; }

    /**
     * Override to render.
     */
    @Override
    public void renderAll(Painter aPainter)
    {
        // Make sure OpenGL is initialized
        if (_gl == null) {
            initCanvas((TVPainter) aPainter);
            if (_gl == null)
                return;
        }

        // Make sure canvas is still right size
        else resizeCanvas((TVPainter) aPainter);

        // Get GL and clear
        _gl.clear(_gl.COLOR_BUFFER_BIT | _gl.DEPTH_BUFFER_BIT);

        // Iterate over scene shapes and render each
        Scene3D scene = getScene();
        renderShape3D(scene);

        // Paint WebGL canvas to painter
        TVPainter tvPainter = (TVPainter) aPainter;
        tvPainter._cntx.drawImage(_canvas, 0, 0, _canvasW, _canvasH);
    }

    /**
     * Renders a Shape3D.
     */
    protected void renderShape3D(Shape3D aShape3D)
    {
        // If shape not visible, just return
        if (!aShape3D.isVisible())
            return;

        // Handle Parent: Iterate over children and recurse
        if (aShape3D instanceof ParentShape) {
            ParentShape parentShape = (ParentShape) aShape3D;
            Shape3D[] children = parentShape.getChildren();
            for (Shape3D child : children)
                renderShape3D(child);
        }

        // Handle child: Get VertexArray and render
        else {
            VertexArray vertexArray = aShape3D.getVertexArray();
            while (vertexArray != null) {
                renderVertexArray(vertexArray);
                vertexArray = vertexArray.getNext();
            }
        }
    }

    /**
     * Renders a VertexBuffer of triangles.
     */
    protected void renderVertexArray(VertexArray aVertexArray)
    {
        // If VertexArray.DoubleSided, disable face culling
        boolean doubleSided = aVertexArray.isDoubleSided();
        if (doubleSided)
            _gl.disable(_gl.CULL_FACE);

        // Get ShaderProgram
        WebGLProgram program = getProgram(aVertexArray);

        // Use this program
        _gl.useProgram(program);

        // Set program Projection Matrix (was program.setProjectionMatrix(projMatrix) )
        Camera3D camera = getCamera();
        double[] projMatrix = camera.getCameraToClipArray();
        Float32Array matrix4fv = TV.getFloat32Array(projMatrix);
        WebGLUniformLocation projMatrixUniform = _gl.getUniformLocation(program, "projMatrix");
        _gl.uniformMatrix4fv(projMatrixUniform, false, matrix4fv);

        // Set program ViewMatrix (was program.setViewMatrix(viewMatrix) )
        double[] sceneToCamera = camera.getSceneToCameraArray();
        Float32Array viewMatrix4fv = TV.getFloat32Array(sceneToCamera);
        WebGLUniformLocation viewMatrixUniform = _gl.getUniformLocation(program, "viewMatrix");
        _gl.uniformMatrix4fv(viewMatrixUniform, false, viewMatrix4fv);

        // Create pointsBuffer
        WebGLBuffer pointsBuffer = _gl.createBuffer();
        _gl.bindBuffer(_gl.ARRAY_BUFFER, pointsBuffer);

        // Set VertexArray.pointsArray in pointsBuffer (was program.setPoints(pointsArray) )
        float[] pointsArray = aVertexArray.getPointArray();
        _gl.bufferData(_gl.ARRAY_BUFFER, TV.getFloat32Array(pointsArray), _gl.STATIC_DRAW);
        int pointsAttrLoc = _gl.getAttribLocation(program, "vertPoint");
        _gl.vertexAttribPointer(pointsAttrLoc, 3, _gl.FLOAT, false, 3 * 4, 0);
        _gl.enableVertexAttribArray(pointsAttrLoc);

        // If color array present, set colors
        WebGLBuffer colorsBuffer = null;
        int colorsAttrLoc = 0;
        if (aVertexArray.isColorArraySet()) {

            // Create colorsBuffer
            colorsBuffer = _gl.createBuffer();
            _gl.bindBuffer(_gl.ARRAY_BUFFER, colorsBuffer);

            // Set VertexArray.colorsArray in colorsBuffer (was program.setColors(colorsArray) )
            float[] colorsArray = aVertexArray.getColorArray();
            _gl.bufferData(_gl.ARRAY_BUFFER, TV.getFloat32Array(colorsArray), _gl.STATIC_DRAW);
            colorsAttrLoc = _gl.getAttribLocation(program, "vertColor");
            _gl.vertexAttribPointer(colorsAttrLoc, 3, _gl.FLOAT, false, 3 * 4, 0);
            _gl.enableVertexAttribArray(colorsAttrLoc);
        }

        // Otherwise, set VertexArray color (was program.setColor(color) )
        else {
            Color color = aVertexArray.getColor(); if (color == null) color = Color.RED;
            WebGLUniformLocation colorUniform = _gl.getUniformLocation(program, "vertColor");
            float[] colorArray = { (float) color.getRed(), (float) color.getGreen(), (float) color.getBlue() };
            _gl.uniform3fv(colorUniform, colorArray);
        }

        // Run program
        int vertexCount = pointsArray.length / 3;
        _gl.drawArrays(_gl.TRIANGLES, 0, vertexCount);

        // Delete buffers
        _gl.deleteBuffer(pointsBuffer);
        _gl.disableVertexAttribArray(pointsAttrLoc);
        if (colorsBuffer != null) {
            _gl.deleteBuffer(colorsBuffer);
            _gl.disableVertexAttribArray(colorsAttrLoc);
        }

        // Restore
        if (doubleSided)
            _gl.enable(_gl.CULL_FACE);
    }

    /**
     * Returns a ShaderProgram for VertexArray.
     */
    public WebGLProgram getProgram(VertexArray aVertexArray)
    {
        // If shader exists, return
        String name = getShaderString(aVertexArray);
        WebGLProgram program = _programs.get(name);
        if (program != null)
            return program;

        // Create, set and return
        program = _gl.createProgram();
        WebGLShader vertexShader = getVertexShader(name);
        WebGLShader fragmentShader = getFragmentShader(name);
        _gl.attachShader(program, vertexShader);
        _gl.attachShader(program, fragmentShader);

        // Link Program
        _gl.linkProgram(program);

        // Validate
        _gl.validateProgram(program);
        //JSObject linkStatus = _gl.getProgramParameter(program, _gl.LINK_STATUS);
        //if ( ! linkStatus) {
        //    var info = gl.getProgramInfoLog(program);
        //    throw 'Could not compile WebGL program. \n\n' + info;
        //}

        _programs.put(name, program);
        return program;
    }

    /**
     * Returns a VertexShader for given VertexArray.
     */
    public WebGLShader getVertexShader(String name)
    {
        // If shader exists, return
        WebGLShader shader = _vertShaders.get(name);
        if (shader != null)
            return shader;

        // Create, set and return
        shader = _gl.createShader(_gl.VERTEX_SHADER);
        String sourceText = getSourceText(_gl.VERTEX_SHADER, name);
        _gl.shaderSource(shader, sourceText);
        _gl.compileShader(shader);
        _vertShaders.put(name, shader);
        return shader;
    }

    /**
     * Returns a Fragment Shader for given VertexArray.
     */
    public WebGLShader getFragmentShader(String name)
    {
        // If shader exists, return
        WebGLShader shader = _fragShaders.get(name);
        if (shader != null)
            return shader;

        // Create, set and return
        shader = _gl.createShader(_gl.FRAGMENT_SHADER);
        String sourceText = getSourceText(_gl.FRAGMENT_SHADER, name);
        _gl.shaderSource(shader, sourceText);
        _gl.compileShader(shader);
        _fragShaders.put(name, shader);
        return shader;
    }

    /**
     * Returns a unique string.
     */
    public String getShaderString(VertexArray aVertexArray)
    {
        boolean hasColors = aVertexArray.isColorArraySet();
        return hasColors ? "Points_Colors" : "Points_Color";
    }

    /**
     * Returns the full text string of shader file.
     */
    public String getSourceText(int aType, String aName)
    {
        String sourcePath = "shaders/" + getSourceName(aType, aName);
        return SnapUtils.getText(getClass(), sourcePath);
    }

    /**
     * Returns the shader file name.
     */
    public String getSourceName(int aType, String aName)
    {
        // Handle Vertex Shaders:
        if (aType == _gl.VERTEX_SHADER && aName.equals("Points_Color"))
            return "Points_Color.vs";
        if (aType == _gl.VERTEX_SHADER && aName.equals("Points_Colors"))
            return "Points_Colors.vs";

        // Handle Fragment Shaders
        if (aType == _gl.FRAGMENT_SHADER)
            return "General.fs";

        // Something went wrong
        return null;
    }

    /**
     * Registers factory.
     */
    public static void registerFactory()
    {
        // If already set, just return
        for (RendererFactory factory : RendererFactory.getFactories())
            if (factory.getClass() == TVRendererFactory.class)
                return;

        // Create, add and setDefault
        RendererFactory joglFactory = new TVRendererFactory();
        RendererFactory.addFactory(joglFactory);
        RendererFactory.setDefaultFactory(joglFactory);
    }

    /**
     * A RendererFactory implementation for RendererJogl.
     */
    public static class TVRendererFactory extends RendererFactory {

        /**
         * Returns the renderer name.
         */
        public String getRendererName()  { return "WebGL"; }

        /**
         * Returns a new default renderer.
         */
        public Renderer newRenderer(Camera3D aCamera)
        {
            return new TVRenderer(aCamera);
        }
    }
}
