
// https://eldenring.wiki.fextralife.com/Bosses
Array.prototype.slice.call(document.querySelectorAll('#wiki-content-block > .tabcontent.tabcurrent .row > .col-sm-4'))
  .map(
    col => ({
      location: col.querySelector('h4.special > a').textContent,
      bosses: Array.prototype.slice.call(col.querySelectorAll('li'))
        .map(li => li.textContent)
    })
  )
  .flatMap(row =>
    row.bosses.map(boss =>
      ({
        type: 'BOSS',
        groupName: row.location,
        name: boss
      })
      )
  )

  // https://eldenring.wiki.fextralife.com/Sites+of+Grace
  Array.prototype.slice.call(document.querySelectorAll('#wiki-content-block h3.special'))
      .flatMap(groupEl =>
        Array.prototype.slice.call(
            groupEl.nextElementSibling.tagName === 'UL'
              ? groupEl.nextElementSibling.querySelectorAll('li')
              : Array.prototype.slice.call(groupEl.nextElementSibling.querySelectorAll('ul'))
                  .flatMap(ulEl => Array.prototype.slice.call(ulEl.querySelectorAll('li')))
        )
            .map(liEl => {
              const groupName = groupEl.querySelector('a').textContent
              const name = liEl.textContent
                .replaceAll(/\s*\[map link\]/gi, '')
                .replaceAll(/\s*\(the roundtable is only.*$/gi, '')
              return {
                type: 'BONFIRE',
                groupName,
                name,
                id: `${name} (${groupName})`
              }
            })
      )
