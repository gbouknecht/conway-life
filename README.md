While rereading _The Recursive Universe: Cosmic Complexity and the Limits of
Scientific Knowledge_ written by William Poundstone in 1985, I decided to give
[Conway's Game of Life](http://www.conwaylife.com/) a try
in [Clojure](https://clojure.org).

To start, install [Leiningen](https://leiningen.org) and run `lein run`.

| Key      | Mode     | Description                                                |
|----------|----------|------------------------------------------------------------|
| `-`      |          | zoom out                                                   |
| `=`, `+` |          | zoom in                                                    |
| `0`      |          | set zoom to 1 pixel per cel                                |
| `r`      |          | show raster                                                |
| `s`      |          | start/stop                                                 |
| `n`      |          | next                                                       |
| `N`      |          | previous                                                   |
| `C`      |          | clear board                                                |
| `c`      |          | move board origin to center of window and cursor to origin |
| `left`   | :running | move board left                                            |
| `left`   | :stopped | move cursor left                                           |
| `right`  | :running | move board right                                           |
| `right`  | :stopped | move cursor right                                          |
| `up`     | :running | move board up                                              |
| `up`     | :stopped | move cursor up                                             |
| `down`   | :running | move board down                                            |
| `down`   | :stopped | move cursor down                                           |
| `space`  | :stopped | toggle cell at cursor                                      |

| Mouse        | Mode     | Description                           |
|--------------|----------|---------------------------------------|
| single click | :stopped | toggle cell and move cursor to cell   |
| double click | :stopped | toggle cell and move cursor to cell   |
| double click | :running | move clicked cell to center of window |

## License

Copyright © 2023 Gerrit Bouknecht

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
