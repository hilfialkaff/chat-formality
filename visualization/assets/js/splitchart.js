var w = 960,
    h = 500,
    p = [20, 50, 30, 20],
    x = d3.scale.ordinal().rangeRoundBands([0, w - p[1] - p[3]]),
    y = d3.scale.linear().range([0, h - p[0] - p[2]]),
    z = d3.scale.linear().domain([0, 40, 65, 100]).range([d3.rgb(255, 255, 255), d3.rgb(255, 191, 38), d3.rgb(151, 189, 94), d3.rgb(26, 56, 56)]);
z2 = d3.scale.linear().domain([0, 40, 65, 100]).range([d3.rgb(255, 255, 255), d3.rgb(252, 202, 80), d3.rgb(252, 157, 28), d3.rgb(158, 51, 46)]);
parse = d3.time.format("%Y-%m-%d").parse,
    format = d3.time.format("%b");

var svg = d3.select("#graph").append("svg:svg")
    .attr("width", w)
    .attr("height", h)
    .append("svg:g")
    .attr("transform", "translate(" + p[3] + "," + (h - p[2]) + ")");

d3.csv("jordan_split.csv", function (crimea) {

    // Transpose the data into layers by cause.
    var causes = d3.layout.stack()(["sent_freq", "rec_freq"].map(function (cause) {
        return crimea.map(function (d) {
            //console.log(d)
            console.log(cause)
            var tempcolor = d.rec_formality;
            var temptype = "sent"
            if (cause == "sent_freq") {
                temptype = "rec";
                tempcolor = d.sent_formality;
            }
            tempobj = {x: parse(d.date), y: +d[cause], c: tempcolor, type: temptype};
//console.log(tempobj)
//console.log(tempobj);
            console.log("iteration")
            return tempobj;
        });
    }));

// Compute the x-domain (by date) and y-domain (by top).
    x.domain(causes[0].map(function (d) {
        return d.x;
    }));
    y.domain([0, d3.max(causes[causes.length - 1], function (d) {
        return d.y0 + d.y;
    })]);

// Add a group for each cause.
    var cause = svg.selectAll("g.cause")
        .data(causes)
        .enter().append("svg:g")
        .attr("class", "cause");

    console.log(cause)

// Add a rect for each date.
    var rect = cause.selectAll("rect")
        .data(Object)
        .enter().append("svg:rect")
        .attr("x", function (d) {
            return x(d.x);
        })
        .attr("y", function (d) {
            return -y(d.y0) - y(d.y);
        })
        .attr("height", function (d) {
            return y(d.y);
        })
        .attr("width", x.rangeBand())
        .attr("fill", function (d) {
            if (d.type == "sent") {
                return z(d.c);
            }
            return z2(d.c);
        })
        .style("stroke", function (d, i) {
            return d3.rgb(210, 210, 210);
        });


});


