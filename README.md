# Altitude
Simple example app to show how to use DEM services to calculate a correct altitude.

FYT/Dudu calculates the altitude for a certain location but they do not apply a DEM service.<br>
You do need a DEM (Digital Elevation Model) service to do an altitude correction as our Earth is not a smooth ball.<br>For explanation see [this Wikipedia](https://en.wikipedia.org/wiki/Digital_elevation_model) article.

There are several DEM services available:

   -  Open-Elevation API
   -  Google Elevation API
   -  Mapbox Terrain API
   -  OpenTopoData

This example app uses the Open-Elevation API (of course).

I hope that DuDu/FYT will implement this functionality.


<HR>

Copyleft 2026 Harry van der Wolf (surfer63), MIT License.<br>


## MIT License
Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
