
DefaultSize = 48
DefaultFont = "#{DefaultSize}px 'sans-serif'"
DefaultColor = '#006400'

class @SeaMap
  constructor: (idTag) ->
    @tag = $('#' + idTag)
    @positions = []
    @cellInfos = {}
    @toAlpha = []
    @layers = []
    layerUrl = @tag.attr('data-layer')
    if layerUrl
      that = @
      $.getJSON layerUrl, (data) ->
        that.layers = data
        image = new MapImage(idTag, that.layers)
        image.onclick = that.runClick
        that.image = image
        that.load()
    else
      @image = new MapImage(idTag, @layers)
      @image.onclick = @runClick
      @load()

  load: () ->
    posUrl = @tag.attr('data-position')
    infoUrl = @tag.attr('data-cellinfo')
    that = @
    @image.onload = () ->
      $.when(
        $.getJSON(posUrl),
        if infoUrl then $.getJSON(infoUrl) else null
      ).done (posData, infoData) ->
        posData[0].forEach (d) ->
          that.positions[d.cell] = {x: d.posX, y: d.posY}
        if infoData
          infoData[0].forEach (d) ->
            that.cellInfos[d.alphabet] = d.cell
            that.toAlpha[d.cell] = d.alphabet

        if that.layers.length == 0
          return that.onload()

        labelUrl = that.tag.attr('data-labelpos')
        layerCount = 0
        that.layers.forEach (s) ->
          $.when(
            $.getJSON(posUrl, {suffix: s}),
            $.getJSON(labelUrl, {suffix: s})
          ).done (posData, labelData) ->
            posData[0].forEach (d) ->
              that.positions[d.cell] = {x: d.posX, y: d.posY}
              frameName = d.routeName ? 'route_'+d.cell+'_1'
              if d.routeX && d.routeY
                that.image.setLayerPoint(s, frameName, {x: d.posX + d.routeX, y: d.posY + d.routeY})
            labelData[0].forEach (d) ->
              that.image.setLayerPoint(s, d.imageName, {x: d.posX, y: d.posY})
            layerCount++
            if that.layers.length == layerCount
              that.image.setLayers()
              that.onload()

  setPoint: (cell, fixed) ->
    if fixed
      @fix = cell
    @image.clear()
    pos = @getPos(cell)
    @image.setPoint(pos.x, pos.y)

  setLine: (start, end) ->
    @image.clear()
    start = @getPos(start)
    end = @getPos(end)
    @image.setLine(start.x, start.y, end.x, end.y)

  getPos: (cell) ->
    p = @positions[cell]
    p ?= @positions[@cellInfos[cell]]
    p

  runClick: (x, y) =>
    cell = _.findIndex @positions, (pos) ->
      (pos.x - DefaultSize / 2) < x and
        x < (pos.x + DefaultSize / 2) and
        (pos.y - DefaultSize / 2) < y and
        y < (pos.y + DefaultSize / 2)
    alpha = @toAlpha[cell]
    @onclick(alpha)

  onload: ->

  onclick: (alpha) ->

  clear: ->
    @image.clear()
    if @fix
      @setPoint(@fix, false)


class MapImage
  constructor: (idTag, layers) ->
    @tag = $('#' + idTag)
    @layers = layers
    @layerFrames = []
    @layerImgs = []
    @ctx = @tag[0].getContext('2d')
    @frames = []
    @loadImage()
    that = @
    @tag.click (event) ->
      dElm = document.documentElement
      dBody = document.body
      nX = dElm.scrollLeft || dBody.scrollLeft
      nY = dElm.scrollTop || dBody.scrollTop
      that.onclick(event.clientX - @offsetLeft + nX, event.clientY - @offsetTop + nY)

  loadImage: () ->
    @imageUrl = @tag.attr('data-src')
    @bgImg = new Image()
    that = @
    @bgImg.onload = () ->
      if that.layers.length == 0
        that.setImage()
        that.onload()
      else
        that.loadLayerImages()

    @frameUrl = @tag.attr('data-frame')
    @bgLayers = []
    if @frameUrl
      @bgUrl = @tag.attr('data-background')
      that = @
      $.when(
        $.getJSON(@frameUrl),
        $.getJSON(@bgUrl)
      ).done (frameData, bgData) ->
        frameData[0].forEach (d) ->
          that.frames[d.name] = {pos: {x: d.posX, y: d.posY}, width: d.width, height: d.height}
        that.bgLayers = bgData[0].map (bg) -> bg.imageName
        that.bgImg.src = that.imageUrl
    else
      @bgImg.src = @imageUrl

  loadLayerImages: () ->
    @layerImgs = []
    layerCount = 0
    that = @
    @layers.forEach (s) ->
      img = new Image()
      img.onload = () ->
        layerCount++
        if that.layers.length == layerCount
          that.setImage()
          that.onload()

      that.layerFrames[s] = {}
      that.layerImgs[s] = img
      $.getJSON that.frameUrl, {suffix: s}, (data) ->
        data.forEach (d) ->
          that.layerFrames[s][d.name] = {pos: {x: d.posX, y: d.posY}, width: d.width, height: d.height}
        img.src = that.imageUrl + '?suffix=' + s

  setImage: () ->
    @ctx.globalAlpha = 1.0
    that = @
    if @bgLayers.length != 0
      @bgLayers.forEach (name) ->
        frame = that.frames[name]
        that.ctx.drawImage(that.bgImg, frame.pos.x, frame.pos.y, frame.width, frame.height, 0, 0, frame.width, frame.height)
    else
      @ctx.drawImage(@bgImg, 0, 0, @bgImg.width, @bgImg.height, 0, 0, @bgImg.width, @bgImg.height)

  setLayers: () ->
    that = @
    @layers.forEach (s) ->
      that.setLayer(s)

  setLayer: (s) ->
    @ctx.globalAlpha = 1.0
    that = @
    Object.keys(@layerFrames[s]).forEach (name) ->
      frame = that.layerFrames[s][name]
      if frame.point
        that.ctx.drawImage(that.layerImgs[s], frame.pos.x, frame.pos.y, frame.width, frame.height, frame.point.x, frame.point.y, frame.width, frame.height)

  setLayerPoint: (s, frameName, point) ->
    if @layerFrames[s].hasOwnProperty frameName
      @layerFrames[s][frameName].point = point

  setPoint: (x, y) ->
    @ctx.font = DefaultFont
    @ctx.globalAlpha = 0.8
    @ctx.fillStyle = DefaultColor
    @ctx.fillText('★', x - DefaultSize / 2, y + DefaultSize / 3.5)

  setLine: (x1, y1, x2, y2) ->
    @ctx.fillStyle = DefaultColor
    @ctx.strokeStyle = DefaultColor
    arrow(@ctx, x1, y1, x2, y2, 20, 40, 30, 7)

  clear: () ->
    @setImage()
    @setLayers()

  onload: () ->
    @setImage()
    @setLayers()

  onclick: (x, y) ->

arrow = (ctx, ax, ay, bx, by_, w, h, h2, width) ->
  vx = bx - ax
  vy = by_ - ay
  v = Math.sqrt(vx * vx + vy * vy)
  ux = vx / v
  uy = vy / v
  lx = bx - uy * w - ux * h
  ly = by_ + ux * w - uy * h
  rx = bx + uy * w - ux * h
  ry = by_ - ux * w - uy * h
  mx = bx - ux * h2
  my = by_ - uy * h2
  line(ctx, ax, ay, bx - ux * width, by_ - uy * width, width)
  triangle(ctx, bx, by_, lx, ly, mx, my)
  triangle(ctx, bx, by_, rx, ry, mx, my)

line = (ctx, x1, y1, x2, y2, width) ->
  ctx.beginPath()
  ctx.lineWidth = width
  ctx.moveTo(x1, y1)
  ctx.lineTo(x2, y2)
  ctx.stroke()

triangle = (ctx, x1, y1, x2, y2, x3, y3) ->
  path = new Path2D()
  path.moveTo(x1, y1)
  path.lineTo(x2, y2)
  path.lineTo(x3, y3)
  ctx.fill(path)
