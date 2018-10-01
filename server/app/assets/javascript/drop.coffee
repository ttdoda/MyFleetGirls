
$(document).ready ->
  area = $('#stage').attr('data-area')
  info = $('#stage').attr('data-info')
  seaMap = new SeaMap('map_image')
  $('.panel').each ->
    elem = $(this)
    id = elem.attr('id')
    cell = elem.attr('data-cell')
    is1st = elem.attr('data-1st') ? false
    new Vue(vueConf(elem, id, cell, is1st))
  $('.panel-heading').each ->
    elem = $(this)
    cell = elem.attr('data-cell')
    elem.hover setPoint(seaMap, cell), () -> seaMap.clear()
  obj = fromURLParameter(location.hash.replace(/^\#/, ''))
  $('.collapse').on 'show.bs.collapse', ->
    cell = $(@).parent().attr('data-cell').split('-', 3)[2]
    seaMap.setPoint(cell, true)
  $('.collapse').on 'hide.bs.collapse', ->
    here = location.href.replace(/\#.*$/, '') # hash以下を削除
    history.replaceState(null, null, here)
  seaMap.onload = ->
    $("#collapse#{obj.cell}").collapse()
  seaMap.onclick = (alpha) ->
    $('.collapse').each ->
      $(this).collapse('hide')
    $("#collapse#{area}-#{info}-#{alpha}").collapse('show')

setPoint = (seaMap, cell) ->
  () ->
    seaMap.setPoint(cell, false)

timeout = 0

vueConf = (elem, id, cell, is1st) ->
  el: '#' + id

  data:
    drops: []
    dropOnly: false
    rank_s: true
    rank_a: true
    rank_b: true
    rank_n: false
    map_rank_all: true
    map_rank_ko: true
    map_rank_otsu: true
    map_rank_hei: true
    url: ''
    period: true
    from: moment({year: 2014, month: 0, day: 1}).format('YYYY-MM-DD')
    to: moment().format('YYYY-MM-DD')

  methods:
    rank: ->
      (if @rank_s then 'S' else '') +
        (if @rank_a then 'A' else '') +
        (if @rank_b then 'B' else '') +
        (if @rank_n then 'N' else '')
    mapRank: ->
      if @map_rank_all then ''
      else
        (if @map_rank_ko then 'ko' else '') +
          (if @map_rank_otsu then 'otsu' else '') +
          (if @map_rank_hei then 'hei' else '')
    getJSON: ->
      @setHash()
      url = decodeURIComponent(@url.replace('(rank)', @rank()))
      url = url.replace('(mapRank)', @mapRank())
      if @period
        url = url.replace('(from)', @from)
        url = url.replace('(to)', @to)
      $.getJSON url, (data) =>
        xs = if @dropOnly then _.filter(data, (drop) -> drop.getShipName?) else data
        @drops = xs.reverse().map (it) ->
          it.getShipName ?= 'ドロップ無し'
          it
        @drops.sort (x, y) -> y.count - x.count
    draw: ->
      countSum = @countUpDrops(@drops)
      typed = _.groupBy @drops, (drop) -> drop.getShipType
      types = for type, ships of typed
        if type == 'undefined'
          count = ships[0].count
          name: "ドロップ無し #{@viewCount(count, countSum)}", count: count
        else
          sum = @countUpDrops(ships)
          children = ships.map (ship) =>
            name: "#{ship.getShipName} #{@viewCount(ship.count, countSum)}"
            count: ship.count
          name: "#{type} #{@viewCount(sum, countSum)}", children: children
      data = name: "ALL #{countSum}(100%)", children: types
      id = '#' + elem.find('.sunburst').attr('id')
      $(id).empty()
      drawSunburst(900, 600, id, data)
    countUpDrops: (drops) ->
      counts = drops.map (drop) -> drop.count
      _.reduce counts, (x, y) -> x + y
    viewCount: (elem, sum) -> "#{elem}(#{Math.round(elem/sum*1000)/10}%)"
    setHash: ->
      obj = {cell: cell, rank: @rank(), dropOnly: @dropOnly}
      if @period
        obj.from = @from
        obj.to = @to
      location.hash = toURLParameter(obj)
    restoreHash: ->
      obj = fromURLParameter(location.hash.replace(/^\#/, ''))
      if obj.cell?
        @dropOnly = obj.dropOnly == 'true'
      if obj.rank?
        @rank_s = obj.rank.indexOf('S') != -1
        @rank_a = obj.rank.indexOf('A') != -1
        @rank_b = obj.rank.indexOf('B') != -1
        @rank_n = obj.rank.indexOf('N') != -1
      if obj.from?
        @period = true
        @from = obj.from
        @to = obj.to ? @to
    clickDrop: (drop) ->
      base = "/entire/sta/from_ship#query=#{drop.getShipName}&ship=#{drop.getShipId}"
      url = base + if @period then "&from=#{@from}&to=#{@to}" else ''
      location.href = url


  created: ->
    if is1st
      @to = moment({year: 2018, month: 7, day: 16}).format('YYYY-MM-DD')
    else
      @from = moment({year: 2018, month: 7, day: 16}).format('YYYY-MM-DD')
    i = this
    elem.find('.panel-collapse').on 'show.bs.collapse', ->
      i.restoreHash()
      i.url = $(this).attr('data-url')
      if i.drops.length == 0
        i.getJSON()
      else
        i.setHash()

  watch:
    dropOnly: -> @getJSON()
    rank_s: -> @getJSON()
    rank_a: -> @getJSON()
    rank_b: -> @getJSON()
    rank_n: -> @getJSON()
    map_rank_all: -> @getJSON()
    map_rank_ko: -> @getJSON()
    map_rank_otsu: -> @getJSON()
    map_rank_hei: -> @getJSON()
    drops: (drops) ->
      $("#panel#{cell}")[0].scrollIntoView(true)
      @draw()
    period: -> @getJSON()
    from: ->
      if @period
        clearTimeout(timeout)
        timeout = setTimeout(@getJSON, 500)
    to: ->
      if @period
        clearTimeout(timeout)
        timeout = setTimeout(@getJSON, 500)
