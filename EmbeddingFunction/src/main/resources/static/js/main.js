document.addEventListener('DOMContentLoaded', function() {
    // Initialize charts
    initializeCharts();
    
    // Initialize embedding visualization
    initializeEmbeddingVisualization();
    
    // Add event listeners
    addEventListeners();
});

function initializeCharts() {
    // API Requests Chart
    const apiRequestsCtx = document.getElementById('api-requests-chart').getContext('2d');
    const apiRequestsChart = new Chart(apiRequestsCtx, {
        type: 'line',
        data: {
            labels: ['Mar 1', 'Mar 2', 'Mar 3', 'Mar 4', 'Mar 5', 'Mar 6', 'Mar 7', 'Mar 8', 'Mar 9', 'Mar 10', 
                     'Mar 11', 'Mar 12', 'Mar 13', 'Mar 14', 'Mar 15', 'Mar 16', 'Mar 17', 'Mar 18', 'Mar 19', 'Mar 20',
                     'Mar 21', 'Mar 22', 'Mar 23', 'Mar 24', 'Mar 25', 'Mar 26', 'Mar 27', 'Mar 28', 'Mar 29', 'Mar 30'],
            datasets: [{
                label: 'API Requests',
                data: [28500, 29200, 27800, 28100, 29500, 30200, 31000, 29800, 28700, 27500, 
                       26800, 27200, 28500, 29800, 30500, 31200, 32000, 33500, 34200, 35000,
                       34500, 33800, 32500, 31800, 32200, 33500, 34800, 35500, 36200, 37000],
                borderColor: '#0066CC',
                backgroundColor: 'rgba(0, 102, 204, 0.1)',
                borderWidth: 2,
                tension: 0.4,
                fill: true
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: false
                },
                tooltip: {
                    mode: 'index',
                    intersect: false
                }
            },
            scales: {
                x: {
                    grid: {
                        display: false
                    }
                },
                y: {
                    beginAtZero: true,
                    grid: {
                        color: 'rgba(0, 0, 0, 0.05)'
                    }
                }
            }
        }
    });

    // Embedding Types Chart
    const embeddingTypesCtx = document.getElementById('embedding-types-chart').getContext('2d');
    const embeddingTypesChart = new Chart(embeddingTypesCtx, {
        type: 'doughnut',
        data: {
            labels: ['Financial Profile', 'Trading Pattern', 'Compliance Pattern', 'Client Effort', 'Other'],
            datasets: [{
                data: [35, 25, 20, 15, 5],
                backgroundColor: [
                    '#0066CC',
                    '#4D94FF',
                    '#00AA55',
                    '#FFAA00',
                    '#FF5555'
                ],
                borderWidth: 0
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'right',
                    labels: {
                        boxWidth: 12,
                        padding: 15
                    }
                }
            },
            cutout: '70%'
        }
    });

    // Response Time Chart
    const responseTimeCtx = document.getElementById('response-time-chart').getContext('2d');
    const responseTimeChart = new Chart(responseTimeCtx, {
        type: 'bar',
        data: {
            labels: ['<10ms', '10-25ms', '25-50ms', '50-100ms', '>100ms'],
            datasets: [{
                label: 'Response Time Distribution',
                data: [15, 30, 40, 10, 5],
                backgroundColor: '#0066CC',
                borderRadius: 4
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: false
                }
            },
            scales: {
                x: {
                    grid: {
                        display: false
                    }
                },
                y: {
                    beginAtZero: true,
                    grid: {
                        color: 'rgba(0, 0, 0, 0.05)'
                    },
                    ticks: {
                        callback: function(value) {
                            return value + '%';
                        }
                    }
                }
            }
        }
    });
}

function initializeEmbeddingVisualization() {
    const canvas = document.getElementById('embedding-canvas');
    if (!canvas) return;

    // Sample data for visualization
    const data = generateSampleEmbeddingData();
    
    // Initialize visualization based on selected type
    const visualizationType = document.getElementById('visualization-type').value;
    renderVisualization(canvas, data, visualizationType);
}

function generateSampleEmbeddingData() {
    // Generate sample data for embedding visualization
    const data = [];
    const clientTypes = ['Conservative', 'Moderate', 'Aggressive', 'Income-focused', 'Growth-focused'];
    const riskLevels = ['Low', 'Medium-Low', 'Medium', 'Medium-High', 'High'];
    
    for (let i = 0; i < 100; i++) {
        const clientType = clientTypes[Math.floor(Math.random() * clientTypes.length)];
        const riskLevel = riskLevels[Math.floor(Math.random() * riskLevels.length)];
        
        // Generate x, y coordinates based on client type and risk level
        let x, y;
        
        switch (clientType) {
            case 'Conservative':
                x = Math.random() * 0.3;
                break;
            case 'Moderate':
                x = 0.3 + Math.random() * 0.4;
                break;
            case 'Aggressive':
                x = 0.7 + Math.random() * 0.3;
                break;
            case 'Income-focused':
                x = Math.random() * 0.5;
                break;
            case 'Growth-focused':
                x = 0.5 + Math.random() * 0.5;
                break;
        }
        
        switch (riskLevel) {
            case 'Low':
                y = Math.random() * 0.3;
                break;
            case 'Medium-Low':
                y = 0.3 + Math.random() * 0.2;
                break;
            case 'Medium':
                y = 0.5 + Math.random() * 0.2;
                break;
            case 'Medium-High':
                y = 0.7 + Math.random() * 0.15;
                break;
            case 'High':
                y = 0.85 + Math.random() * 0.15;
                break;
        }
        
        // Add some random noise
        x = Math.max(0, Math.min(1, x + (Math.random() - 0.5) * 0.1));
        y = Math.max(0, Math.min(1, y + (Math.random() - 0.5) * 0.1));
        
        data.push({
            id: `client-${i + 1}`,
            name: `Client ${i + 1}`,
            x: x,
            y: y,
            clientType: clientType,
            riskLevel: riskLevel,
            investmentSize: 10000 + Math.floor(Math.random() * 990000)
        });
    }
    
    return data;
}

function renderVisualization(canvas, data, type) {
    const ctx = canvas.getContext('2d');
    const width = canvas.width = canvas.parentElement.clientWidth;
    const height = canvas.height = canvas.parentElement.clientHeight;
    
    // Clear canvas
    ctx.clearRect(0, 0, width, height);
    
    // Draw background grid
    drawGrid(ctx, width, height);
    
    // Draw data points
    drawDataPoints(ctx, data, width, height, type);
    
    // Draw axes and labels
    drawAxes(ctx, width, height, type);
}

function drawGrid(ctx, width, height) {
    ctx.strokeStyle = 'rgba(0, 0, 0, 0.05)';
    ctx.lineWidth = 1;
    
    // Draw vertical grid lines
    for (let x = 0; x <= width; x += width / 10) {
        ctx.beginPath();
        ctx.moveTo(x, 0);
        ctx.lineTo(x, height);
        ctx.stroke();
    }
    
    // Draw horizontal grid lines
    for (let y = 0; y <= height; y += height / 10) {
        ctx.beginPath();
        ctx.moveTo(0, y);
        ctx.lineTo(width, y);
        ctx.stroke();
    }
}

function drawDataPoints(ctx, data, width, height, type) {
    // Draw data points based on visualization type
    if (type === '2d' || type === undefined) {
        // 2D scatter plot
        data.forEach(point => {
            const x = point.x * width;
            const y = (1 - point.y) * height; // Invert y-axis
            const radius = Math.log(point.investmentSize / 10000) * 2 + 3;
            
            // Determine color based on client type
            let color;
            switch (point.clientType) {
                case 'Conservative':
                    color = '#0066CC';
                    break;
                case 'Moderate':
                    color = '#00AA55';
                    break;
                case 'Aggressive':
                    color = '#FF5555';
                    break;
                case 'Income-focused':
                    color = '#FFAA00';
                    break;
                case 'Growth-focused':
                    color = '#9966CC';
                    break;
                default:
                    color = '#999999';
            }
            
            // Draw point
            ctx.beginPath();
            ctx.arc(x, y, radius, 0, Math.PI * 2);
            ctx.fillStyle = color;
            ctx.globalAlpha = 0.7;
            ctx.fill();
            ctx.globalAlpha = 1.0;
            ctx.strokeStyle = 'rgba(255, 255, 255, 0.8)';
            ctx.lineWidth = 1;
            ctx.stroke();
        });
    } else if (type === 'cluster') {
        // Cluster view - group by client type
        const clusters = {};
        data.forEach(point => {
            if (!clusters[point.clientType]) {
                clusters[point.clientType] = [];
            }
            clusters[point.clientType].push(point);
        });
        
        // Draw clusters
        Object.keys(clusters).forEach((clusterName, index) => {
            const clusterPoints = clusters[clusterName];
            const centerX = width * (0.2 + (index / (Object.keys(clusters).length - 1)) * 0.6);
            const centerY = height / 2;
            
            // Determine color based on cluster name
            let color;
            switch (clusterName) {
                case 'Conservative':
                    color = '#0066CC';
                    break;
                case 'Moderate':
                    color = '#00AA55';
                    break;
                case 'Aggressive':
                    color = '#FF5555';
                    break;
                case 'Income-focused':
                    color = '#FFAA00';
                    break;
                case 'Growth-focused':
                    color = '#9966CC';
                    break;
                default:
                    color = '#999999';
            }
            
            // Draw cluster label
            ctx.font = 'bold 14px sans-serif';
            ctx.fillStyle = '#333333';
            ctx.textAlign = 'center';
            ctx.fillText(clusterName, centerX, centerY - 100);
            
            // Draw cluster points
            clusterPoints.forEach(point => {
                const angle = Math.random() * Math.PI * 2;
                const distance = Math.random() * 80;
                const x = centerX + Math.cos(angle) * distance;
                const y = centerY + Math.sin(angle) * distance;
                const radius = Math.log(point.investmentSize / 10000) * 2 + 3;
                
                // Draw point
                ctx.beginPath();
                ctx.arc(x, y, radius, 0, Math.PI * 2);
                ctx.fillStyle = color;
                ctx.globalAlpha = 0.7;
                ctx.fill();
                ctx.globalAlpha = 1.0;
                ctx.strokeStyle = 'rgba(255, 255, 255, 0.8)';
                ctx.lineWidth = 1;
                ctx.stroke();
            });
        });
    }
}

function drawAxes(ctx, width, height, type) {
    ctx.strokeStyle = '#666666';
    ctx.lineWidth = 2;
    ctx.font = '12px sans-serif';
    ctx.fillStyle = '#666666';
    
    if (type === '2d' || type === undefined) {
        // X-axis
        ctx.beginPath();
        ctx.moveTo(0, height);
        ctx.lineTo(width, height);
        ctx.stroke();
        
        // X-axis label
        ctx.textAlign = 'center';
        ctx.fillText('Investment Style (Conservative → Aggressive)', width / 2, height - 10);
        
        // Y-axis
        ctx.beginPath();
        ctx.moveTo(0, 0);
        ctx.lineTo(0, height);
        ctx.stroke();
        
        // Y-axis label
        ctx.save();
        ctx.translate(15, height / 2);
        ctx.rotate(-Math.PI / 2);
        ctx.textAlign = 'center';
        ctx.fillText('Risk Tolerance (Low → High)', 0, 0);
        ctx.restore();
    }
}

function addEventListeners() {
    // Visualization type change
    const visualizationTypeSelect = document.getElementById('visualization-type');
    if (visualizationTypeSelect) {
        visualizationTypeSelect.addEventListener('change', function() {
            initializeEmbeddingVisualization();
        });
    }
    
    // Analytics timeframe change
    const analyticsTimeframeSelect = document.getElementById('analytics-timeframe');
    if (analyticsTimeframeSelect) {
        analyticsTimeframeSelect.addEventListener('change', function() {
            // In a real application, this would fetch new data based on the selected timeframe
            console.log('Analytics timeframe changed to:', this.value);
        });
    }
    
    // Make table rows clickable
    const tableRows = document.querySelectorAll('.embeddings-table tbody tr');
    tableRows.forEach(row => {
        row.addEventListener('click', function() {
            // In a real application, this would navigate to the embedding details page
            console.log('Row clicked:', this.querySelector('td').textContent);
        });
    });
    
    // Window resize event for responsive charts
    window.addEventListener('resize', function() {
        // Reinitialize charts on window resize
        initializeCharts();
        initializeEmbeddingVisualization();
    });
} 