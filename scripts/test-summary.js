const fs = require('fs');
const path = require('path');

function generateTestSummary() {
    const cucumberReportPath = path.join(__dirname, '../target/cucumber-reports/cucumber.json');
    const allureResultsPath = path.join(__dirname, '../target/allure-results');
    
    console.log('📋 Test Execution Summary');
    console.log('='.repeat(50));
    
    // Read Cucumber JSON report
    if (fs.existsSync(cucumberReportPath)) {
        try {
            const data = fs.readFileSync(cucumberReportPath, 'utf8');
            const report = JSON.parse(data);
            const total = report.length;
            let passed = 0;
            let failed = 0;
            
            report.forEach(feature => {
                feature.elements.forEach(scenario => {
                    const allPassed = scenario.steps.every(step => 
                        step.result && step.result.status === 'passed');
                    if (allPassed) {
                        passed++;
                    } else {
                        failed++;
                    }
                });
            });
            
            console.log(`\n📊 Cucumber Report:`);
            console.log(`   Total Scenarios: ${total}`);
            console.log(`   Passed: ${passed} (${total > 0 ? Math.round((passed/total)*100) : 0}%)`);
            console.log(`   Failed: ${failed} (${total > 0 ? Math.round((failed/total)*100) : 0}%)`);
            
            // List failed scenarios
            if (failed > 0) {
                console.log(`\n❌ Failed Scenarios:`);
                report.forEach(feature => {
                    feature.elements.forEach(scenario => {
                        const allPassed = scenario.steps.every(step => 
                            step.result && step.result.status === 'passed');
                        if (!allPassed) {
                            console.log(`   - ${scenario.name}`);
                        }
                    });
                });
            }
        } catch (error) {
            console.log(`   Error reading Cucumber report: ${error.message}`);
        }
    } else {
        console.log(`\n📊 Cucumber Report: Not found at ${cucumberReportPath}`);
    }
    
    // Check Allure results
    if (fs.existsSync(allureResultsPath)) {
        const files = fs.readdirSync(allureResultsPath);
        const testFiles = files.filter(f => f.endsWith('.json')).length;
        console.log(`\n🌟 Allure Results:`);
        console.log(`   Test Results: ${testFiles} files`);
    } else {
        console.log(`\n🌟 Allure Results: Not found at ${allureResultsPath}`);
    }
    
    console.log('\n📁 Report Locations:');
    console.log('   Cucumber HTML: target/cucumber-reports/cucumber-report.html');
    console.log('   Allure Report: target/allure-report/index.html');
    console.log('   Screenshots: target/screenshots/');
    console.log('='.repeat(50));
}

// Run if called directly
if (require.main === module) {
    generateTestSummary();
}

module.exports = { generateTestSummary };