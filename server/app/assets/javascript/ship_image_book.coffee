$(document).ready ->
  vue = new Vue(vueConf)

vueConf =
  el: '#images'

  data:
    ships: []

  methods:
    imageUrl: (id) ->
      shipImage = jsRoutes.controllers.RestImage.ship2nd(id, "card")
      shipImage.url
    getShips: ->
      bookShipsUrl = jsRoutes.controllers.RestUser.bookShips(@userId).url
      $.get bookShipsUrl, (data) =>
        @ships = data

  watch:
    userId: -> @getShips()
