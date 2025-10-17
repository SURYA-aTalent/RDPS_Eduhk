var dashboardUtil = {
	_barChart: function(element, bardata, title, height, animate, easing, duration, delay, color) {
		if (typeof d3 == 'undefined') {
			console.warn('Warning - d3.min.js is not loaded.');
			return;
		}

		// Initialize chart only if element exsists in the DOM
		if ($(element).length > 0) {

			// Basic setup
			// ------------------------------

			// Add data set
// var bardata = [];
// for (var i = 0; i < barQty; i++) {
// bardata.push(Math.round(Math.random() * 10) + 10);
// }

			// Main variables
			var d3Container = d3.select(element), width = d3Container.node().getBoundingClientRect().width;
			
			d3Container.selectAll('svg').remove();

			// Construct scales
			// ------------------------------

			// Horizontal
			var x = d3.scale.ordinal().rangeBands([ 0, width ], 0.3);

			// Vertical
			var y = d3.scale.linear().range([ 0, height ]);

			// Set input domains
			// ------------------------------

			// Horizontal
			x.domain(d3.range(0, bardata.length));

			// Vertical
			y.domain([ 0, d3.max(bardata) ]);

			// Create chart
			// ------------------------------

			// Add svg element
			var container = d3Container.append('svg');

			// Add SVG group
			var svg = container.attr('width', width).attr('height', height).append('g');

			//
			// Append chart elements
			//

			// Bars
			var bars = svg.selectAll('rect').data(bardata).enter().append('rect').attr('class', 'd3-random-bars').attr('width', x.rangeBand()).attr('x', function(d, i) {
				return x(i);
			}).style('fill', color);

			// Tooltip
			// ------------------------------

			var tip = d3.tip().attr('class', 'd3-tip').offset([ -10, 0 ]);

			// Show and hide
			bars.call(tip).on('mouseover', tip.show).on('mouseout', tip.hide);

			tip.html(function(d, i) {
				return '<div class="text-center">' + '<h6 class="m-0">' + d + '</h6><div class="font-size-sm">' + title[i] + '</div>' + '</div>'
			});

			// Bar loading animation
			// ------------------------------

			// Choose between animated or static
			if (animate) {
				withAnimation();
			} else {
				withoutAnimation();
			}

			// Animate on load
			function withAnimation() {
				bars.attr('height', 0).attr('y', height).transition().attr('height', function(d) {
					return y(d);
				}).attr('y', function(d) {
					return height - y(d);
				}).delay(function(d, i) {
					return i * delay;
				}).duration(duration).ease(easing);
			}

			// Load without animateion
			function withoutAnimation() {
				bars.attr('height', function(d) {
					return y(d);
				}).attr('y', function(d) {
					return height - y(d);
				})
			}

			// Resize chart
			// ------------------------------

			// Call function on window resize
			$(window).on('resize', barsResize);

			// Call function on sidebar width change
			$(document).on('click', '.sidebar-control', barsResize);

			// Resize function
			// 
			// Since D3 doesn't support SVG resize by default,
			// we need to manually specify parts of the graph that need to
			// be updated on window resize
			function barsResize() {

				// Layout variables
				width = d3Container.node().getBoundingClientRect().width;

				// Layout
				// -------------------------

				// Main svg width
				container.attr('width', width);

				// Width of appended group
				svg.attr('width', width);

				// Horizontal range
				x.rangeBands([ 0, width ], 0.3);

				// Chart elements
				// -------------------------

				// Bars
				svg.selectAll('.d3-random-bars').attr('width', x.rangeBand()).attr('x', function(d, i) {
					return x(i);
				});
			}
		}
	},
	_progressIcon: function(element, radius, border, backgroundColor, foregroundColor, end, iconClass) {
        if (typeof d3 == 'undefined') {
            console.warn('Warning - d3.min.js is not loaded.');
            return;
        }

        // Initialize chart only if element exsists in the DOM
        if(element) {


            // Basic setup
            // ------------------------------

            // Main variables
            var d3Container = d3.select(element),
                startPercent = (+d3.select(".progress-percentage").text().replace("%", "")) / 100 || 0,
                iconSize = 32,
                endPercent = end,
                twoPi = Math.PI * 2,
                formatPercent = d3.format('.0%'),
                boxSize = radius * 2;

            d3Container.selectAll('svg').remove();
            
            // Values count
            var count = Math.abs((endPercent - startPercent) / 0.01);

            // Values step
            var step = endPercent < startPercent ? -0.01 : 0.01;


            // Create chart
            // ------------------------------

            // Add SVG element
            var container = d3Container.append('svg');

            // Add SVG group
            var svg = container
                .attr('width', boxSize)
                .attr('height', boxSize)
                .append('g')
                    .attr('transform', 'translate(' + (boxSize / 2) + ',' + (boxSize / 2) + ')');


            // Construct chart layout
            // ------------------------------

            // Arc
            var arc = d3.svg.arc()
                .startAngle(0)
                .innerRadius(radius)
                .outerRadius(radius - border)
                .cornerRadius(20);


            //
            // Append chart elements
            //

            // Paths
            // ------------------------------

            // Background path
            svg.append('path')
                .attr('class', 'd3-progress-background')
                .attr('d', arc.endAngle(twoPi))
                .style('fill', backgroundColor);

            // Foreground path
            var foreground = svg.append('path')
                .attr('class', 'd3-progress-foreground')
                .attr('filter', 'url(#blur)')
                .style({
                    'fill': foregroundColor,
                    'stroke': foregroundColor
                });

            // Front path
            var front = svg.append('path')
                .attr('class', 'd3-progress-front')
                .style({
                    'fill': foregroundColor,
                    'fill-opacity': 1
                });


            // Text
            // ------------------------------

            // Percentage text value
            var numberText = d3.select('.progress-percentage')
                    .attr('class', 'pt-1 mt-2 mb-1 progress-percentage');

            // Icon
            d3.select(element)
                .append("i")
                    .attr("class", iconClass + " counter-icon")
                    .style({
                        'color': foregroundColor,
                        'top': ((boxSize - iconSize) / 2) + 'px'
                    });


            // Animation
            // ------------------------------

            // Animate path
            function updateProgress(progress) {
            	if (progress >= 0) {
            		foreground.attr('d', arc.endAngle(twoPi * progress));
            		front.attr('d', arc.endAngle(twoPi * progress));
                	numberText.text(formatPercent(progress));	
                }
            }

            // Animate text
            var progress = startPercent;
            (function loops() {
                updateProgress(progress);
                if (count > 0) {
                    count--;
                    progress += step;
                    setTimeout(loops, 10);
                }
            })();
        }
    }	
}