// Simple Chart.js alternative for pie charts
class SimpleChart {
    constructor(canvas, options) {
        this.canvas = canvas;
        this.ctx = canvas.getContext('2d');
        this.options = options || {};
        this.data = null;
    }

    setData(data) {
        this.data = data;
        this.render();
    }

    render() {
        if (!this.data || !this.data.datasets || !this.data.datasets[0]) {
            return;
        }

        const dataset = this.data.datasets[0];
        const labels = this.data.labels || [];
        const values = dataset.data || [];
        const colors = dataset.backgroundColor || [];

        // Clear canvas
        this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);

        // Calculate total
        const total = values.reduce((sum, value) => sum + value, 0);
        if (total === 0) return;

        // Draw pie chart
        const centerX = this.canvas.width / 2;
        const centerY = this.canvas.height / 2;
        const radius = Math.min(centerX, centerY) - 20;

        let currentAngle = -Math.PI / 2; // Start from top

        for (let i = 0; i < values.length; i++) {
            const value = values[i];
            const percentage = value / total;
            const sliceAngle = percentage * 2 * Math.PI;

            // Draw slice
            this.ctx.beginPath();
            this.ctx.moveTo(centerX, centerY);
            this.ctx.arc(centerX, centerY, radius, currentAngle, currentAngle + sliceAngle);
            this.ctx.closePath();
            this.ctx.fillStyle = colors[i] || '#cccccc';
            this.ctx.fill();
            this.ctx.strokeStyle = '#ffffff';
            this.ctx.lineWidth = 2;
            this.ctx.stroke();

            // Draw label
            const labelAngle = currentAngle + sliceAngle / 2;
            const labelX = centerX + Math.cos(labelAngle) * (radius * 0.7);
            const labelY = centerY + Math.sin(labelAngle) * (radius * 0.7);

            this.ctx.fillStyle = '#333333';
            this.ctx.font = '12px Arial';
            this.ctx.textAlign = 'center';
            this.ctx.textBaseline = 'middle';
            this.ctx.fillText(Math.round(percentage * 100) + '%', labelX, labelY);

            currentAngle += sliceAngle;
        }

        // Draw legend
        this.drawLegend(labels, colors, values, total);
    }

    drawLegend(labels, colors, values, total) {
        const legendX = 20;
        const legendY = 20;
        const itemHeight = 20;
        const colorBoxSize = 15;

        this.ctx.font = '12px Arial';
        this.ctx.textAlign = 'left';
        this.ctx.textBaseline = 'middle';

        for (let i = 0; i < labels.length; i++) {
            const y = legendY + i * itemHeight;

            // Draw color box
            this.ctx.fillStyle = colors[i] || '#cccccc';
            this.ctx.fillRect(legendX, y - colorBoxSize/2, colorBoxSize, colorBoxSize);

            // Draw label
            this.ctx.fillStyle = '#333333';
            const percentage = Math.round((values[i] / total) * 100);
            this.ctx.fillText(`${labels[i]} (${percentage}%)`, legendX + colorBoxSize + 5, y);
        }
    }
}

// Global Chart object to mimic Chart.js API
window.Chart = function(ctx, config) {
    if (config.type === 'pie') {
        const chart = new SimpleChart(ctx.canvas, config.options);
        chart.setData(config.data);
        return chart;
    }
    throw new Error('Only pie charts are supported by this simple implementation');
};

// Add static properties to mimic Chart.js
Chart.register = function() {};
Chart.defaults = {
    global: {
        responsive: true,
        maintainAspectRatio: false
    }
};
