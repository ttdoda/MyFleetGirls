
DefaultSize = 48
DefaultFont = "#{DefaultSize}px 'sans-serif'"
DefaultColor = '#006400'

class @SeaMap
  constructor: (idTag) ->
    layerUrl = $('#' + idTag).attr('data-layer')
    @layers = []
    that = @
    $.ajax
      url: layerUrl
      async: false
      dataType: 'json'
      success: (data) ->
        that.layers = data
    @tag = $('#' + idTag)
    @positions = []
    @cellInfos = {}
    @toAlpha = []
    @image = new MapImage(idTag, @layers)
    @image.onclick = @runClick
    @load(idTag)

  load: (idTag) ->
    posUrl = @tag.attr('data-position')
    infoUrl = @tag.attr('data-cellinfo')
    that = @
    @image.onload = () ->
      deferr = []
      d = new $.Deferred
      $.getJSON posUrl, (data) ->
        data.forEach (d) ->
          that.positions[d.cell] = {x: d.posX, y: d.posY}
        if infoUrl
          $.getJSON infoUrl, (data) ->
            data.forEach (d) ->
              that.cellInfos[d.alphabet] = d.cell
              that.toAlpha[d.cell] = d.alphabet
            d.resolve
        else
          d.resolve
      deferr.push d.promise
      @layers.forEach (s) ->
        d = new $.Deferred
        $.getJSON posUrl + '?suffix=' + s, (data) ->
          data.forEach (d) ->
            that.positions[d.cell] = {x: d.posX, y: d.posY}
            that.image.setLayerPoint(s, d.cell, {name: d.routeName, x: d.posX + d.routeX, y: d.posY + d.routeY})
          that.image.setLayer(s)
          if infoUrl
            $.getJSON infoUrl + '?suffix=' + s, (data) ->
              data.forEach (d) ->
                that.cellInfos[d.alphabet] = d.cell
                that.toAlpha[d.cell] = d.alphabet
              d.resolve
          else
            d.resolve
        deferr.push d.promise
      $.when.apply($,deferr).done ->
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

  runClick: (x, y) ->
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
    @ctx = @tag[0].getContext('2d')
    @layerImgs = []
    frameUrl = @tag.attr('data-frame')
    @frames = []
    bgName = 'map'+@tag.attr('data-area')+'-'+@tag.attr('data-info')
    @bgLayers = [bgName, bgName+'_point']
    that = @
    $.ajax
      url: frameUrl
      async: false
      dataType: 'json'
      success: (data) ->
        data.forEach (d) ->
          that.frames[d.name] = {pos: {x: d.posX, y: d.posY}, width: d.width, height: d.height}
    @layerFrames = []
    @layers.forEach (s) ->
      $.getJSON frameUrl + '?suffix=' + s, (data) ->
        that.layerFrames[s] = {}
        data.forEach (d) ->
          that.layerFrames[s][d.name] = {pos: {x: d.posX, y: d.posY}, width: d.width, height: d.height}
    @loadImage()
    @tag.click (event) ->
      dElm = document.documentElement
      dBody = document.body
      nX = dElm.scrollLeft || dBody.scrollLeft
      nY = dElm.scrollTop || dBody.scrollTop
      that.onclick(event.clientX - @offsetLeft + nX, event.clientY - @offsetTop + nY)

  loadImage: () ->
    imageUrl = @tag.attr('data-src')
    @bgImg = new Image()
    @layerImgs = []
    that = @
    @bgImg.onload = () ->
      that.setImage()
      layerCount = 0
      that.layers.forEach (s) ->
        img = new Image()
        img.onload = ->
          layerCount++
          if layerCount >= that.layers.length
            that.onload()
        img.src = imageUrl + '?suffix=' + s
        that.layerImgs[s] = img
    @bgImg.src = imageUrl

  setImage: () ->
    @ctx.globalAlpha = 1.0
    that = @
    @bgLayers.forEach (name) ->
      frame = that.frames[name]
      that.ctx.drawImage(that.bgImg, frame.pos.x, frame.pos.y, frame.width, frame.height, 0, 0, frame.width, frame.height)

  setLayers: () ->
    that = @
    @layers.forEach (s) ->
      that.setLayer(s)

  setLayer: (s) ->
    @ctx.globalAlpha = 1.0
    that = @
    Object.keys(@layerFrames[s]).forEach (name) ->
      console.log(name)
      if name.match(/route_\d+_1/)
        frame = that.layerFrames[s][name]
        that.ctx.drawImage(that.layerImgs[s], frame.pos.x, frame.pos.y, frame.width, frame.height, frame.point.x, frame.point.y, frame.width, frame.height)
      return

  setLayerPoint: (s, cell, point) ->
    frameName = point.name ? 'route_'+cell+'_1'
    if @layerFrames[s].hasOwnProperty frameName
      Object.assign @layerFrames[s][frameName], {point: {x: point.x, y: point.y}}

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
