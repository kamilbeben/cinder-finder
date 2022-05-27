
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
      
      // `INSERT INTO room_location (type, group_name, name) VALUES ('BOSS', '${location.location.replace('\'', '\'\'')}', '${boss.replace('\'', '\'\'')}');`
      ({
        type: 'BOSS',
        groupName: row.location,
        name: boss
      })
      )
  ).join('\n')